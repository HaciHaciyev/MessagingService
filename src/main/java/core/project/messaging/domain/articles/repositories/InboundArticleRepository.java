package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.values_objects.ArticleTag;

import java.util.Set;

public interface InboundArticleRepository {

    void save(Article article);

    void save(Set<ArticleTag> articleTags);
}
