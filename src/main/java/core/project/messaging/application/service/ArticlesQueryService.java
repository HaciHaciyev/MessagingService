package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticlePreview;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.ArticlesQueryForm;
import core.project.messaging.domain.user.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static core.project.messaging.application.util.JsonUtilities.responseException;

@ApplicationScoped
public class ArticlesQueryService {

    private final OutboundArticleRepository articleRepository;

    ArticlesQueryService(OutboundArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<ArticlePreview> pageOf(ArticlesQueryForm query) {
        return articleRepository.page(query).orElseThrow(() -> new IllegalArgumentException("Invalid query"));
    }

    public List<ArticlePreview> pageOf(int pageNumber, int pageSize, String username) {
        if (!Username.validate(username)) {
            throw responseException(Response.Status.BAD_REQUEST, "Invalid username");
        }

        return articleRepository.page(pageNumber, pageSize, username)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not exists"));
    }

    public List<Article> archive(int pageNumber, int pageSize, String username) {
        if (!Username.validate(username)) {
            throw responseException(Response.Status.BAD_REQUEST, "Invalid username");
        }

        return articleRepository.archive(pageNumber, pageSize, username)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not exists"));
    }

    public List<Article> draft(int pageNumber, int pageSize, String username) {
        if (!Username.validate(username)) {
            throw responseException(Response.Status.BAD_REQUEST, "Invalid username");
        }

        return articleRepository.draft(pageNumber, pageSize, username)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not exists"));
    }
}
