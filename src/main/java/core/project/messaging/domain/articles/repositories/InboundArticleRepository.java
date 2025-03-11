package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
import core.project.messaging.domain.articles.values_objects.Like;
import core.project.messaging.domain.user.value_objects.Username;

import java.util.UUID;

public interface InboundArticleRepository {

    void save(Article article);

    void updateViews(View view);

    void deleteView(UUID articleID, Username username);

    void updateLikes(Like like);

    void deleteLike(UUID uuid, Username username);
}
