package core.project.messaging.domain.articles.repositories;

import core.project.messaging.application.dto.ArticlePreview;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.values_objects.ArticlesQueryForm;
import core.project.messaging.domain.commons.containers.Result;

import java.util.List;
import java.util.UUID;

public interface OutboundArticleRepository {

    Result<Article, Throwable> article(UUID id);

    boolean isViewExists(UUID articleID, UUID userID);

    boolean isArticleExists(UUID articleID);

    Result<List<ArticlePreview>, Throwable> page(ArticlesQueryForm query);

    Result<List<ArticlePreview>, Throwable> page(int pageNumber, int pageSize, String username);

    Result<List<Article>, Throwable> archive(int pageNumber, int pageSize, String username);

    Result<List<Article>, Throwable> draft(int pageNumber, int pageSize, String username);
}
