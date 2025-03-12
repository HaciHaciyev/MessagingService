package core.project.messaging.infrastructure.dal.repository.outbound;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.events.ArticleEvents;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import core.project.messaging.domain.articles.values_objects.Body;
import core.project.messaging.domain.articles.values_objects.Header;
import core.project.messaging.domain.articles.values_objects.Summary;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.select;

@Transactional
@ApplicationScoped
public class JdbcOutboundArticleRepository implements OutboundArticleRepository {

    private final JDBC jdbc;

    static final String ARTICLE = select()
            .column("a.id").as("id")
            .column("a.author_id").as("author_id")
            .column("a.header").as("header")
            .column("a.summary").as("summary")
            .column("a.body").as("body")
            .column("a.status").as("status")
            .column("a.creation_date").as("creation_date")
            .column("a.last_updated").as("last_updated")
            .count("v.id").as("views")
            .count("l.article_id").as("likes")
            .from("Articles a")
            .join("Views v", "v.article_id = a.id")
            .join("Likes l", "l.article_id = a.id")
            .where("a.id = ?")
            .build();

    static final String ARTICLE_TAGS = select()
            .column("tag")
            .from("ArticlesTags")
            .where("article_id = ?")
            .build();

    static final String VIEW = select()
            .count("user_id")
            .from("Views")
            .where("article_id = ?")
            .and("user_id = ?")
            .build();

    static final String IS_ARTICLE_EXISTS = select()
            .count("id")
            .from("Articles")
            .where("id = ?")
            .build();

    JdbcOutboundArticleRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isViewExists(UUID articleID, UUID userID) {
        return jdbc.readObjectOf(VIEW, Integer.class, articleID.toString(), userID.toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking view existence.");
                    return false;
                });
    }

    @Override
    public boolean isArticleExists(UUID articleID) {
        return jdbc.readObjectOf(IS_ARTICLE_EXISTS, Integer.class, articleID.toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking article existence.");
                    return false;
                });
    }

    @Override
    public Result<Article, Throwable> article(UUID articleID) {
        Result<List<ArticleTag>, Throwable> articleTags = jdbc.readListOf(ARTICLE_TAGS, this::articleTagsMapper, articleID.toString());
        if (!articleTags.success()) {
            return Result.failure(articleTags.throwable());
        }

        Result<Article, Throwable> article = jdbc.read(ARTICLE, this::articleMapper, articleID.toString());
        if (!article.success()) {
            return Result.failure(article.throwable());
        }

        articleTags.value().forEach(articleTag -> article.value().addTag(articleTag));
        return article;
    }

    private ArticleTag articleTagsMapper(ResultSet rs) throws SQLException {
        return new ArticleTag(rs.getString("tag"));
    }

    private Article articleMapper(ResultSet rs) throws SQLException {
        ArticleEvents events = new ArticleEvents(
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("last_updated", Timestamp.class).toLocalDateTime()
        );

        return Article.fromRepository(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("author_id")),
                new HashSet<>(),
                rs.getLong("views"),
                rs.getLong("likes"),
                new Header(rs.getString("header")),
                new Summary(rs.getString("summary")),
                new Body(rs.getString("body")),
                ArticleStatus.valueOf(rs.getString("status")),
                events
        );
    }
}
