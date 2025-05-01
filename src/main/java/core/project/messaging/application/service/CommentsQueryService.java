package core.project.messaging.application.service;

import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static core.project.messaging.application.util.JSONUtilities.responseException;

@ApplicationScoped
public class CommentsQueryService {

    private final OutboundCommentRepository commentRepository;

    CommentsQueryService(OutboundCommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> pageOf(String articleID, int pageNumber, int pageSize) {
        try {
            return commentRepository
                    .page(UUID.fromString(articleID), pageNumber, pageSize)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Can`t found a comments."));
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Comment> pageOf(String articleID, String parentCommentID, int pageNumber, int pageSize) {
        try {
            return commentRepository
                    .page(UUID.fromString(articleID), UUID.fromString(parentCommentID), pageNumber, pageSize)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Can`t found a comments."));
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }
}
