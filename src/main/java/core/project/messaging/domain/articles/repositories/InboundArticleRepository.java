package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;

public interface InboundArticleRepository {

    void save(Article article);

    void updateViews(View view);
}
