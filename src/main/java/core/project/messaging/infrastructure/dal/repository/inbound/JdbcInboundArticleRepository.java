package core.project.messaging.infrastructure.dal.repository.inbound;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
import core.project.messaging.domain.articles.events.ArticleUpdatedEvent;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.Like;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.*;

@Transactional
@ApplicationScoped
public class JdbcInboundArticleRepository implements InboundArticleRepository {

    private final JDBC jdbc;

    static final String SAVE_ARTICLE = insert()
            .into("Article")
            .columns("id",
                    "author_id",
                    "header",
                    "summary",
                    "body",
                    "status",
                    "creation_date",
                    "last_updated"
            )
            .values(8)
            .build();

    static final String SAVE_ARTICLE_TAGS = String.format("%s; %s;",
            insert()
            .into("Tags")
            .columns("tag")
            .values(1)
            .onConflict("tag")
            .doNothing()
            .build(),
            insert()
            .into("ArticleTags")
            .columns("article_id", "tag")
            .values(2)
            .build()
    );

    static final String ARTICLE_VIEW = insert()
            .into("Views")
            .columns("id", "article_id", "reader_id", "creation_date")
            .values(4)
            .build();

    static final String DELETE_VIEW = delete()
            .from("Views")
            .where("v.article_id = ?")
            .and("v.reader_id = ?")
            .build();

    private static final String ARTICLE_LIKE = insert()
            .into("Likes")
            .columns("article_id", "user_id", "creation_date")
            .values(3)
            .build();

    private static final String DELETE_LIKE = delete()
            .from("Likes")
            .where("article_id = ?")
            .and("user_id = ?")
            .build();

    static final String ARTICLE_STATUS = update("Articles")
            .set("status = ?")
            .where("id = ?")
            .build();

    static final String UPDATE_HEADER = update("Articles")
            .set("header = ?")
            .where("id = ?")
            .build();

    static final String UPDATE_SUMMARY = update("Articles")
            .set("summary = ?")
            .where("id = ?")
            .build();

    static final String UPDATE_BODY = update("Articles")
            .set("body = ?")
            .where("id = ?")
            .build();

    static final String UPDATE_DATE = update("Articles")
            .set("last_updated = ?")
            .where("id = ?")
            .build();

    JdbcInboundArticleRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Article article) {
        String articleID = article.id().toString();
        jdbc.write(SAVE_ARTICLE,
                articleID,
                article.authorId().toString(),
                article.header().value(),
                article.summary().value(),
                article.body().value(),
                article.status().toString(),
                article.events().creationDate(),
                article.events().lastUpdateDate()
        )
                .ifFailure(throwable -> Log.errorf("Error saving article: %s", throwable.getMessage()));

        article.tags().forEach(articleTag -> jdbc.write(SAVE_ARTICLE_TAGS, articleTag.value(), articleID, articleTag.value()));
    }

    @Override
    public void updateViews(View view) {
        jdbc.write(ARTICLE_VIEW,
                view.id().toString(),
                view.articleID().toString(),
                view.readerID().toString(),
                view.viewedData()
        )
                .ifFailure(throwable -> Log.errorf("Error saving view: %s", throwable.getMessage()));
    }

    @Override
    public void deleteView(UUID articleID, UUID reader) {
        jdbc.write(DELETE_VIEW, articleID.toString(), reader.toString())
                .ifFailure(throwable -> Log.errorf("Error deleting view: %s", throwable.getMessage()));
    }

    @Override
    public void updateLikes(Like like) {
        jdbc.write(ARTICLE_LIKE, like.articleID().toString(), like.userId().toString(), like.likedAt())
                .ifFailure(throwable -> Log.errorf("Error deleting like: %s", throwable.getMessage()));
    }

    @Override
    public void deleteLike(UUID articleID, UUID userID) {
        jdbc.write(DELETE_LIKE, articleID.toString(), userID.toString())
                .ifFailure(throwable -> Log.errorf("Error deleting like: %s", throwable.getMessage()));
    }

    @Override
    public void statusChange(Article article) {
        jdbc.write(ARTICLE_STATUS, article.status().toString(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing status: %s", throwable.getMessage()));
    }

    @Override
    public void updateHeader(Article article) {
        jdbc.write(UPDATE_HEADER, article.header().value(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing header: %s", throwable.getMessage()));
    }

    @Override
    public void updateSummary(Article article) {
        jdbc.write(UPDATE_SUMMARY, article.summary().value(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing summary: %s", throwable.getMessage()));
    }

    @Override
    public void updateBody(Article article) {
        jdbc.write(UPDATE_BODY, article.body().value(), article.id().toString())
                .ifFailure(throwable -> Log.errorf("Error changing body: %s", throwable.getMessage()));
    }

    @Override
    public void updateEvent(ArticleUpdatedEvent articleEvent) {
        jdbc.write(UPDATE_DATE, articleEvent.data(), articleEvent.articleID().toString())
                .ifFailure(throwable -> Log.errorf("Error updating article date: %s", throwable.getMessage()));
    }
}
