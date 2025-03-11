package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.services.ArticlesService;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.utilities.containers.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class ArticlesApplicationService {

    private final ArticlesService articlesService;

    private final OutboundArticleRepository outboundArticleRepository;

    ArticlesApplicationService(ArticlesService articlesService, OutboundArticleRepository outboundArticleRepository) {
        this.articlesService = articlesService;
        this.outboundArticleRepository = outboundArticleRepository;
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