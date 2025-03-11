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
            .all()
            .from("Articles")
            .where("id = ?")
            .build();

    static final String ARTICLE_TAGS = select()
            .column("tag")
            .from("ArticlesTags")
            .where("article_id = ?")
            .build();

    JdbcOutboundArticleRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Result<Article, Throwable> findByID(UUID articleID) {
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

    @Override
    public Result<List<Article>, Throwable> page(int pageNumber, int pageSize) {
        return null;
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
                new Header(rs.getString("header")),
                new Summary(rs.getString("summary")),
                new Body(rs.getString("body")),
                ArticleStatus.valueOf(rs.getString("status")),
                events
        );
    }
}
