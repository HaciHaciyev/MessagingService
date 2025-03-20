package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticlePreview;
import core.project.messaging.application.dto.ArticlesQueryForm;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import core.project.messaging.domain.user.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Objects;

import static core.project.messaging.domain.articles.services.ArticlesService.SEARCH_QUERY_MAX_SIZE;
import static core.project.messaging.domain.articles.services.ArticlesService.SEARCH_QUERY_MIN_SIZE;

@ApplicationScoped
public class ArticlesQueryService {

    private final OutboundArticleRepository articleRepository;

    ArticlesQueryService(OutboundArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<ArticlePreview> pageOf(ArticlesQueryForm query, String username) {
        if (!Username.validate(username)) {
            throw new IllegalArgumentException("Invalid username");
        }

        validateQuery(query);
        return articleRepository.page(query, username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid query"));
    }

    public List<ArticlePreview> pageOf(int pageNumber, int pageSize, String username) {
        if (!Username.validate(username)) {
            throw new IllegalArgumentException("Invalid username");
        }

        return articleRepository.page(pageNumber, pageSize, username)
                .orElseThrow(() -> new IllegalArgumentException("User not exists"));
    }

    static void validateQuery(ArticlesQueryForm query) {
        String search = query.searchQuery();
        if (Objects.nonNull(search)) {
            validateSearchQuery(search);
        }

        if (Objects.nonNull(query.authorName()) && !Username.validate(query.authorName())) {
            throw new IllegalArgumentException("Author name is invalid.");
        }

        if (Objects.nonNull(query.tag())) {
            ArticleTag.validate(query.tag());
        }
    }

    static void validateSearchQuery(String search) {
        if (search.isBlank()) {
            throw new IllegalArgumentException("Search is blank");
        }
        if (search.length() < SEARCH_QUERY_MIN_SIZE) {
            throw new IllegalArgumentException("Search is too short: min size 3 characters");
        }
        if (search.length() > SEARCH_QUERY_MAX_SIZE) {
            throw new IllegalArgumentException("Search is too long: max size 64 characters");
        }
    }
}
