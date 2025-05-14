package core.project.messaging.application.service;

import core.project.messaging.application.dto.articles.CommentDTO;
import core.project.messaging.application.dto.articles.CommentForm;
import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import core.project.messaging.domain.articles.services.CommentsService;
import core.project.messaging.infrastructure.mappers.CommentMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static core.project.messaging.application.util.JSONUtilities.responseException;

@ApplicationScoped
public class CommentsApplicationService {

    private final CommentMapper mapper;

    private final CommentsService commentsService;

    private final OutboundCommentRepository commentRepository;

    CommentsApplicationService(CommentMapper mapper,
                               CommentsService commentsService,
                               OutboundCommentRepository commentRepository) {
        this.mapper = mapper;
        this.commentsService = commentsService;
        this.commentRepository = commentRepository;
    }

    public List<Comment> pageOf(String articleID, int pageNumber, int pageSize) {
        return commentRepository
                .page(UUID.fromString(articleID), pageNumber, pageSize)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Can`t found a comments."));
    }

    public List<Comment> pageOf(String articleID, String parentCommentID, int pageNumber, int pageSize) {
        return commentRepository
                .page(UUID.fromString(articleID), UUID.fromString(parentCommentID), pageNumber, pageSize)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Can`t found a comments."));
    }

    public void create(CommentForm commentForm, String username) {
        commentsService.create(commentForm, username);
    }

    public CommentDTO edit(String commentID, String text, String username) {
        Comment comment = commentsService.edit(commentID, text, username);
        return mapper.toDto(comment);
    }

    public void delete(String commentID, String username) {
        commentsService.delete(commentID, username);
    }
}
