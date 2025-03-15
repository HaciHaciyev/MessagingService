package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.dto.ArticleText;
import core.project.messaging.application.dto.ArticlesQueryForm;
import core.project.messaging.application.dto.CommentForm;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import core.project.messaging.domain.articles.services.ArticlesService;
import core.project.messaging.domain.articles.services.CommentsService;
import core.project.messaging.infrastructure.utilities.containers.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static core.project.messaging.application.service.PartnersService.buildLimit;
import static core.project.messaging.application.service.PartnersService.buildOffSet;
import static jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class ArticlesApplicationService {

    private final ArticlesService articlesService;

    private final CommentsService commentsService;

    private final OutboundCommentRepository outboundCommentRepository;

    ArticlesApplicationService(ArticlesService articlesService,
                               CommentsService commentsService,
                               OutboundCommentRepository outboundCommentRepository) {
        this.articlesService = articlesService;
        this.commentsService = commentsService;
        this.outboundCommentRepository = outboundCommentRepository;
    }

    public Article save(ArticleForm articleForm, String username) {
        try {
            return articlesService.save(articleForm, username);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Article viewArticle(String articleID, String username) {
        Result<Article, IllegalArgumentException> article = articlesService.viewArticle(articleID, username);
        if (!article.success()) {
            throw getWebApplicationException(Status.BAD_REQUEST, article.throwable().getMessage());
        }

        return article.value();
    }

    public void deleteView(String articleID, String username) {
        try {
            articlesService.deleteView(articleID, username);
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public void likeArticle(String articleID, String username) {
        try {
            articlesService.likeArticle(articleID, username);
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public void deleteLike(String articleID, String username) {
        try {
            articlesService.deleteLike(articleID, username);
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public void createComment(CommentForm commentForm, String username) {
        try {
            commentsService.createComment(commentForm, username);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public void deleteComment(String commentID, String username) {
        try {
            commentsService.deleteComment(commentID, username);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Comment> commentsPageOf(String articleID, int pageNumber, int pageSize) {
        try {
            int limit = buildLimit(pageSize);
            int offSet = buildOffSet(limit, pageNumber);
            return outboundCommentRepository
                    .page(UUID.fromString(articleID), limit, offSet)
                    .orElseThrow(() -> new WebApplicationException(Response
                            .status(Status.NOT_FOUND)
                            .entity("Can`t found comments")
                            .build()));
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Comment> commentsPageOf(String articleID, String parentCommentID, int pageNumber, int pageSize) {
        try {
            int limit = buildLimit(pageSize);
            int offSet = buildOffSet(limit, pageNumber);
            return outboundCommentRepository
                    .page(UUID.fromString(articleID), UUID.fromString(parentCommentID), limit, offSet)
                    .orElseThrow(() -> new WebApplicationException(Response
                            .status(Status.NOT_FOUND)
                            .entity("Can`t found comments")
                            .build()));
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Comment editComment(String commentID, String text, String username) {
        try {
            return commentsService.edit(commentID, text, username);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Article changeStatus(String articleID, ArticleStatus status, String username) {
        try {
            return articlesService.changeStatus(articleID, status, username);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Article updateArticle(String articleID, ArticleText articleText, String username) {
        try {
            return articlesService.updateArticle(articleID, articleText, username);
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Article> pageOf(String username, int pageNumber, int pageSize, ArticlesQueryForm query) {
        return null;
    }

    private static WebApplicationException getWebApplicationException(Response.Status status, String message) {
        return new WebApplicationException(Response
                .status(status)
                .entity(message)
                .build());
    }
}