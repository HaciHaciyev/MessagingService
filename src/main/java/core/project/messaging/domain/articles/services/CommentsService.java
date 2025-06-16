package core.project.messaging.domain.articles.services;

import core.project.messaging.application.dto.articles.CommentForm;
import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.enumerations.CommentType;
import core.project.messaging.domain.articles.repositories.InboundCommentRepository;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import core.project.messaging.domain.articles.values_objects.*;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class CommentsService {

    private final OutboundUserRepository outboundUserRepository;

    private final InboundCommentRepository inboundCommentRepository;

    private final OutboundCommentRepository outboundCommentRepository;

    private final OutboundArticleRepository outboundArticleRepository;

    CommentsService(OutboundUserRepository outboundUserRepository,
                    InboundCommentRepository inboundCommentRepository,
                    OutboundCommentRepository outboundCommentRepository,
                    OutboundArticleRepository outboundArticleRepository) {

        this.outboundUserRepository = outboundUserRepository;
        this.inboundCommentRepository = inboundCommentRepository;
        this.outboundCommentRepository = outboundCommentRepository;
        this.outboundArticleRepository = outboundArticleRepository;
    }

    public void create(CommentForm commentForm, String username) {
        User user = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalDomainArgumentException("Can`t find a user."));

        UUID articleID = UUID.fromString(commentForm.articleID());
        if (!outboundArticleRepository.isArticleExists(articleID)) {
            throw new IllegalDomainArgumentException("You can`t create a comment for not existed article.");
        }

        UUID parentCommentID = commentForm.parentCommentID() == null ? null : UUID.fromString(commentForm.parentCommentID());
        validateParentCommentID(parentCommentID, articleID);

        UUID respondTo = commentForm.respondTo() == null ? null : UUID.fromString(commentForm.respondTo());
        validateRespondID(respondTo, parentCommentID, articleID);

        inboundCommentRepository.save(Comment.of(
                new CommentIdentifiers(UUID.randomUUID(), user.id(), articleID),
                new CommentText(commentForm.text()),
                new Reference(getCommentType(commentForm), parentCommentID, respondTo)
        ));
    }

    public Comment edit(String commentID, String text, String username) {
        User user = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalDomainArgumentException("Can`t find a user."));

        Comment comment = outboundCommentRepository
                .comment(UUID.fromString(commentID))
                .orElseThrow(() -> new IllegalDomainArgumentException("Can`t find a comment."));

        final boolean isAuthor = user.id().equals(comment.userId());
        if (!isAuthor) {
            throw new IllegalDomainArgumentException("Only author can edit a comment.");
        }

        comment.changeText(new CommentText(text));
        inboundCommentRepository.updateCommentText(comment);
        return comment;
    }

    public void delete(String commentID, String username) {
        User user = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalDomainArgumentException("Can`t find a user."));

        Comment comment = outboundCommentRepository
                .comment(UUID.fromString(commentID))
                .orElseThrow(() -> new IllegalDomainArgumentException("Can`t find a comment."));

        final boolean isAuthor = user.id().equals(comment.userId());
        if (!isAuthor) {
            throw new IllegalDomainArgumentException("Only author can edit a comment.");
        }

        inboundCommentRepository.deleteComment(UUID.fromString(commentID), user.id());
    }

    public void like(String commentID, String username) {
        UUID commentUUID = UUID.fromString(commentID);
        Comment comment = outboundCommentRepository.comment(commentUUID)
                .orElseThrow(() -> new IllegalDomainArgumentException("Comment not exists."));

        comment.incrementLikes();

        User user = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalDomainArgumentException("User not found"));

        inboundCommentRepository.like(new CommentLike(commentUUID, user.id(), LocalDateTime.now()));
    }

    public void deleteLike(String commentID, String username) {
        User user = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalDomainArgumentException("User not found"));

        inboundCommentRepository.deleteLike(UUID.fromString(commentID), user.id());
    }

    private void validateRespondID(UUID respondTo, UUID parentCommentID, UUID articleID) {
        if (Objects.isNull(respondTo)) {
            return;
        }

        Result<Comment, Throwable> respondedCommentInfo = outboundCommentRepository.comment(parentCommentID);
        if (!respondedCommentInfo.success()) {
            throw new IllegalDomainArgumentException("Can`t find responded comment.");
        }

        final boolean isDifferentArticles = !articleID.equals(respondedCommentInfo.value().articleId());
        if (isDifferentArticles) {
            throw new IllegalDomainArgumentException("You can`t reference comment to parent comment from another article.");
        }

        final boolean isChildComment = respondedCommentInfo.value().reference().commentType().equals(CommentType.CHILD);
        if (isChildComment && !isUnderTheSameParentComment(parentCommentID, respondedCommentInfo.value())) {
            throw new IllegalDomainArgumentException("You can`t respond to a child comment with different parent.");
        }
    }

    private boolean isUnderTheSameParentComment(UUID parentCommentID, Comment respondedCommentInfo) {
        if (parentCommentID == null) {
            return respondedCommentInfo.reference().parentCommentID() == null;
        }

        return parentCommentID.equals(respondedCommentInfo.reference().parentCommentID());
    }

    private void validateParentCommentID(UUID parentCommentID, UUID articleID) {
        if (Objects.isNull(parentCommentID)) {
            return;
        }

        Result<CommentInfo, Throwable> parentCommentInfo = outboundCommentRepository.commentInfo(parentCommentID);
        if (!parentCommentInfo.success()) {
            throw new IllegalDomainArgumentException("Can`t find parent comment.");
        }

        final boolean isNotParent = !parentCommentInfo.value().commentType().equals(CommentType.PARENT);
        if (isNotParent) {
            throw new IllegalDomainArgumentException("You can`t reference to non-parent comment.");
        }

        final boolean isDifferentArticles = !articleID.equals(parentCommentInfo.value().commentIdentifiers().articleID());
        if (isDifferentArticles) {
            throw new IllegalDomainArgumentException("You can`t reference comment to parent comment from another article.");
        }
    }

    private static CommentType getCommentType(CommentForm commentForm) {
        return commentForm.parentCommentID() == null ? CommentType.PARENT : CommentType.CHILD;
    }
}
