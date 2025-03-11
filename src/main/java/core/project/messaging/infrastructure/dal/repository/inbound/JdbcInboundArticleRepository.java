package core.project.messaging.infrastructure.dal.repository.inbound;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.Like;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.delete;
import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.insert;

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
            .from("Likes")
            .where("article_id = ?")
            .and("user_id = ?")
            .build();

    private static final String ARTICLE_LIKE = insert()
            .into("Likes")
            .columns("article_id", "user_id", "creation_date")
            .values(3)
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
    public void deleteView(UUID articleID, Username reader) {
        jdbc.write(DELETE_VIEW, articleID.toString(), reader.username())
                .ifFailure(throwable -> Log.errorf("Error deleting view: %s", throwable.getMessage()));
    }

    @Override
    public void updateLikes(Like like) {
        jdbc.write(ARTICLE_LIKE, like.articleID().toString(), like.userId().toString(), like.likedAt())
                .ifFailure(throwable -> Log.errorf("Error deleting like: %s", throwable.getMessage()));
    }
}
