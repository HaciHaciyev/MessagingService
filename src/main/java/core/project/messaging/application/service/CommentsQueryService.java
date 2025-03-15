package core.project.messaging.application.service;

import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static core.project.messaging.application.service.PartnersService.buildLimit;
import static core.project.messaging.application.service.PartnersService.buildOffSet;

@ApplicationScoped
public class CommentsQueryService {

    private final OutboundCommentRepository outboundCommentRepository;

    CommentsQueryService(OutboundCommentRepository outboundCommentRepository) {
        this.outboundCommentRepository = outboundCommentRepository;
    }

    public List<Comment> pageOf(String articleID, int pageNumber, int pageSize) {
        try {
            int limit = buildLimit(pageSize);
            int offSet = buildOffSet(limit, pageNumber);
            return outboundCommentRepository
                    .page(UUID.fromString(articleID), limit, offSet)
                    .orElseThrow(() -> getWebApplicationException(Response.Status.NOT_FOUND, "Can`t found a comments."));
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Comment> pageOf(String articleID, String parentCommentID, int pageNumber, int pageSize) {
        try {
            int limit = buildLimit(pageSize);
            int offSet = buildOffSet(limit, pageNumber);
            return outboundCommentRepository
                    .page(UUID.fromString(articleID), UUID.fromString(parentCommentID), limit, offSet)
                    .orElseThrow(() -> getWebApplicationException(Response.Status.NOT_FOUND, "Can`t found a comments."));
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    private static WebApplicationException getWebApplicationException(Response.Status status, String message) {
        return new WebApplicationException(Response
                .status(status)
                .entity(message)
                .build());
    }
}
