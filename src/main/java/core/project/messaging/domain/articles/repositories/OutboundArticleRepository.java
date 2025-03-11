package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.infrastructure.utilities.containers.Result;

import java.util.UUID;

public interface OutboundArticleRepository {

    Result<Article, Throwable> article(UUID id);
}
