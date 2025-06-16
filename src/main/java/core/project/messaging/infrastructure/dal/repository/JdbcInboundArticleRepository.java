package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import core.project.messaging.domain.articles.values_objects.Like;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.*;

@ApplicationScoped
public class JdbcInboundArticleRepository implements InboundArticleRepository {

    private final JetQuerious jet;

    static final String SAVE_ARTICLE = insert()
            .into("Article")
            .column("id")
            .column("author_id")
            .column("header")
            .column("summary")
            .column("body")
            .column("status")
            .column("creation_date")
            .column("last_updated")
            .values()
            .build()
            .sql();

    static final String SAVE_ARTICLE_TAGS = batchOf(
            insert()
            .into("Tags")
            .columns("tag")
            .values()
            .onConflict("tag")
            .doNothing()
            .build().toSQlQuery(),
            insert()
            .into("ArticleTags")
            .columns("article_id", "tag")
            .values()
            .build().toSQlQuery());

    static final String ARTICLE_VIEW = insert()
            .into("Views")
            .columns("id", "article_id", "reader_id", "creation_date")
            .values()
            .build()
            .sql();

    static final String DELETE_VIEW = delete()
            .from("Views")
            .where("v.article_id = ?")
            .and("v.reader_id = ?")
            .build()
            .sql();

    static final String ARTICLE_LIKE = insert()
            .into("Likes")
            .columns("article_id", "user_id", "creation_date")
            .values()
            .build()
            .sql();

    static final String DELETE_LIKE = delete()
            .from("Likes")
            .where("article_id = ?")
            .and("user_id = ?")
            .build()
            .sql();

    static final String ARTICLE_STATUS = update("Articles")
            .set("status = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_HEADER = update("Articles")
            .set("header = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_SUMMARY = update("Articles")
            .set("summary = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_BODY = update("Articles")
            .set("body = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String REMOVE_ARTICLE_TAG = delete()
            .from("ArticleTags")
            .where("article_id = ?")
            .and("tag = ?")
            .build()
            .toString();

    JdbcInboundArticleRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public void save(Article article) {
        String articleID = article.id().toString();
        jet.write(SAVE_ARTICLE,
                        articleID,
                        article.authorId().toString(),
                        article.header().value(),
                        article.summary().value(),
                        article.body().value(),
                        article.status().name(),
                        article.events().creationDate(),
                        article.events().lastUpdateDate())
                .ifFailure(throwable -> Log.errorf("Error saving article: %s", throwable.getMessage()));

        article.tags().forEach(articleTag ->
                jet.write(SAVE_ARTICLE_TAGS, articleID, articleTag.value())
                        .ifFailure(throwable -> Log.errorf("Error adding article tag: %s", throwable.getMessage())));
    }

    @Override
    public void updateViews(View view) {
        jet.write(ARTICLE_VIEW,
                        view.id().toString(),
                        view.articleID().toString(),
                        view.readerID().toString(),
                        view.viewedDate())
                .ifFailure(throwable -> Log.errorf("Error saving view: %s", throwable.getMessage()));
    }

    @Override
    public void deleteView(UUID articleID, UUID reader) {
        jet.write(DELETE_VIEW, articleID.toString(), reader.toString())
                .ifFailure(throwable -> Log.errorf("Error deleting view: %s", throwable.getMessage()));
    }

    @Override
    public void updateLikes(Like like) {
        jet.write(ARTICLE_LIKE, like.articleID().toString(), like.userId().toString(), like.likedAt())
                .ifFailure(throwable -> Log.errorf("Error deleting like: %s", throwable.getMessage()));
    }

    @Override
    public void deleteLike(UUID articleID, UUID userID) {
        jet.write(DELETE_LIKE, articleID.toString(), userID.toString())
                .ifFailure(throwable -> Log.errorf("Error deleting like: %s", throwable.getMessage()));
    }

    @Override
    public void statusChange(Article article) {
        jet.write(ARTICLE_STATUS, article.status().name(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing status: %s", throwable.getMessage()));
    }

    @Override
    public void updateHeader(Article article) {
        jet.write(UPDATE_HEADER, article.header().value(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing header: %s", throwable.getMessage()));
    }

    @Override
    public void updateSummary(Article article) {
        jet.write(UPDATE_SUMMARY, article.summary().value(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing summary: %s", throwable.getMessage()));
    }

    @Override
    public void updateBody(Article article) {
        jet.write(UPDATE_BODY, article.body().value(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing body: %s", throwable.getMessage()));
    }

    @Override
    public void updateTags(Article article) {
        article.tags().forEach(articleTag ->
                jet.write(SAVE_ARTICLE_TAGS, article.id().toString(), articleTag.value())
                        .ifFailure(throwable -> Log.errorf("Error adding article tag: %s", throwable.getMessage())));
    }

    @Override
    public void removeTag(Article article, ArticleTag articleTag) {
        jet.write(REMOVE_ARTICLE_TAG, article.id().toString(), articleTag.value())
                .ifFailure(throwable -> Log.errorf("Error removing article tag: %s", throwable.getMessage()));
    }
}
