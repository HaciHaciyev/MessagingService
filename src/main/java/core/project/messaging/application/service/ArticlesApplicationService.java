package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.services.ArticlesService;
import core.project.messaging.domain.user.entities.UserAccount;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class ArticlesApplicationService {

    private final ArticlesService articlesService;

    private final OutboundUserRepository outboundUserRepository;

    private final OutboundArticleRepository outboundArticleRepository;

    ArticlesApplicationService(ArticlesService articlesService,
                               OutboundUserRepository outboundUserRepository,
                               OutboundArticleRepository outboundArticleRepository) {

        this.articlesService = articlesService;
        this.outboundUserRepository = outboundUserRepository;
        this.outboundArticleRepository = outboundArticleRepository;
    }

    public void save(ArticleForm articleForm, String username) {
        try {
            articlesService.save(articleForm, username);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Article findByID(String articleID, String username) {
        try {
            UserAccount user = outboundUserRepository
                    .findByUsername(new Username(username))
                    .orElseThrow(() -> getWebApplicationException(Status.BAD_REQUEST, "Account is not exists."));

            Article article = outboundArticleRepository
                    .article(UUID.fromString(articleID))
                    .orElseThrow(() -> getWebApplicationException(Status.BAD_REQUEST, "Article is not exists."));

            final boolean isNotPublished = !article.status().equals(ArticleStatus.PUBLISHED);
            final boolean isNotAuthor = !article.authorId().equals(user.getId());

            if (isNotPublished && isNotAuthor) {
                throw getWebApplicationException(Response.Status.NOT_FOUND, "Article not found");
            }

            return article;
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Article> pageOfArticles(int pageNumber, int pageSize, String username) {
        if (!Username.validate(username)) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, "Invalid username");
        }

        return outboundArticleRepository.page(pageNumber, pageSize).orElseThrow(() ->
                getWebApplicationException(Response.Status.BAD_REQUEST, "Can`t find an articles page"));
    }

    private static WebApplicationException getWebApplicationException(Response.Status badRequest, String o) {
        return new WebApplicationException(Response
                .status(badRequest)
                .entity(o)
                .build());
    }
}