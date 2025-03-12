package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.dto.CommentForm;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.services.ArticlesService;
import core.project.messaging.domain.articles.services.CommentsService;
import core.project.messaging.infrastructure.utilities.containers.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class ArticlesApplicationService {

    private final ArticlesService articlesService;

    private final CommentsService commentsService;

    ArticlesApplicationService(ArticlesService articlesService, CommentsService commentsService) {
        this.articlesService = articlesService;
        this.commentsService = commentsService;
    }

    public void save(ArticleForm articleForm, String username) {
        try {
            articlesService.save(articleForm, username);
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

    private static WebApplicationException getWebApplicationException(Response.Status badRequest, String message) {
        return new WebApplicationException(Response
                .status(badRequest)
                .entity(message)
                .build());
    }
}