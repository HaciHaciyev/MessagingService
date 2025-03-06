package core.project.messaging.domain.articles.services;

import core.project.messaging.domain.articles.repositories.ArticleRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArticlesService {

    private final ArticleRepository articleRepository;

    ArticlesService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }


}
