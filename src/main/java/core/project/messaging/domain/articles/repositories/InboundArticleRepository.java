package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import core.project.messaging.domain.articles.values_objects.Like;

import java.util.UUID;

public interface InboundArticleRepository {

    void save(Article article);

    void updateViews(View view);

    void deleteView(UUID articleID, UUID readerID);

    void updateLikes(Like like);

    void deleteLike(UUID articleID, UUID userID);

    void statusChange(Article article);

    void updateHeader(Article article);

    void updateSummary(Article article);

    void updateBody(Article body);

    void updateTags(Article article);

    void removeTag(Article article, ArticleTag articleTag);
}
