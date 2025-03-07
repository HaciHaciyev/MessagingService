package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;

public interface InboundArticleRepository {

    void save(Article article);
}
