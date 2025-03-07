package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.values_objects.CommentText;
import core.project.messaging.domain.articles.values_objects.Reference;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Comment {
    private final UUID id;
    private final UUID userId;
    private final UUID articleId;
    private CommentText text;
    private final Reference reference;
    private final @Nullable UUID respondToComment;

    public Comment(UUID id, UUID userId, UUID articleId, CommentText value, Reference reference, UUID respondToComment) {
        Objects.requireNonNull(id, "ID cannot be null.");
        Objects.requireNonNull(userId, "UserID cannot be null.");
        Objects.requireNonNull(articleId, "ArticleID cannot be null.");
        Objects.requireNonNull(value, "Comment text cannot be null.");
        Objects.requireNonNull(reference, "Reference cannot be null.");

        this.id = id;
        this.userId = userId;
        this.articleId = articleId;
        this.text = value;
        this.reference = reference;
        this.respondToComment = respondToComment;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public UUID articleId() {
        return articleId;
    }

    public CommentText text() {
        return text;
    }

    public void changeText(CommentText commentValue) {
        Objects.requireNonNull(commentValue, "Comment text cannot be null.");
        this.text = commentValue;
    }

    public void delete() {
        this.text = new CommentText(CommentText.DELETED_COMMENT);
    }

    public Reference reference() {
        return reference;
    }

    public Optional<UUID> respondToComment() {
        return Optional.ofNullable(respondToComment);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Comment comment)) return false;

        return id.equals(comment.id) &&
                userId.equals(comment.userId) &&
                articleId.equals(comment.articleId) &&
                Objects.equals(text, comment.text) &&
                reference.equals(comment.reference) &&
                Objects.equals(respondToComment, comment.respondToComment);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + articleId.hashCode();
        result = 31 * result + Objects.hashCode(text);
        result = 31 * result + reference.hashCode();
        result = 31 * result + Objects.hashCode(respondToComment);
        return result;
    }

    @Override
    public String toString() {
        return String.format("""
                Comment {
                    ID: %s,
                    Author ID: %s,
                    Article ID: %s,
                    Text: %s,
                    Referenced on: %s,
                    Referenced comment ID: %s
                    Responded to comment: %s
                }
                """, id, userId, articleId, text.value(), reference.commentType(), reference.parentCommentID(), respondToComment);
    }
}
