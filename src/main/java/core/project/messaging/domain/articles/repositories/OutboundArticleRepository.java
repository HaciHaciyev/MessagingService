package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.infrastructure.utilities.containers.Result;

import java.util.List;
import java.util.UUID;

public interface OutboundArticleRepository {

    Result<Article, Throwable> findByID(UUID id);

    Result<List<Article>, Throwable> page(int pageNumber, int pageSize);
}
