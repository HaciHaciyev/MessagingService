package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticlePreview;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.ArticlesQueryForm;
import core.project.messaging.domain.user.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

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
            throw new IllegalArgumentException("Invalid username");
        }

        return articleRepository.page(pageNumber, pageSize, username)
                .orElseThrow(() -> new IllegalArgumentException("User not exists"));
    }
}
