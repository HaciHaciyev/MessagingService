package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.ChainedWhereBuilder;
import com.hadzhy.jetquerious.sql.Order;
import core.project.messaging.application.dto.articles.ArticlePreview;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.*;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.value_objects.Username;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static com.hadzhy.jetquerious.sql.QueryForge.withAndSelect;

@ApplicationScoped
public class JdbcOutboundArticleRepository implements OutboundArticleRepository {

    private final JetQuerious jet;

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
            .build()
            .sql();

    static final String ARTICLE_TAGS = select()
            .column("tag")
            .from("ArticleTags")
            .where("article_id = ?")
            .build()
            .sql();

    static final String VIEW = select()
            .count("user_id")
            .from("Views")
            .where("article_id = ?")
            .and("user_id = ?")
            .build()
            .sql();

    static final String IS_ARTICLE_EXISTS = select()
            .count("id")
            .from("Articles")
            .where("id = ?")
            .build()
            .sql();

    static final String USER_VIEWS_COUNT = select()
            .count("v.id")
            .from("Views v")
            .join("UserAccount u", "u.username = ?")
            .where("v.reader_id = u.id")
            .build()
            .sql();

    static final String ARTICLES = select()
            .column("a.id").as("id")
            .column("a.author_id").as("author_id")
            .column("a.header").as("header")
            .column("a.summary").as("summary")
            .column("a.status").as("status")
            .column("a.last_updated").as("last_updated")
            .column("ath.firstname").as("firstname")
            .column("ath.lastname").as("lastname")
            .column("ath.username").as("username")
            .count("v.id").as("views")
            .count("l.article_id").as("likes")
            .from("Article a")
            .join("Views v", "v.article_id = a.id")
            .join("Likes l", "l.article_id = a.id")
            .join("UserAccount ath", "ath.id = a.author_id")
            .where("a.status = 'PUBLISHED'")
            .orderBy("COUNT(v.id)", Order.DESC)
            .build()
            .sql();

    static final ChainedWhereBuilder PAGE_OF_ARTICLES = select()
            .column("a.id").as("id")
            .column("a.author_id").as("author_id")
            .column("a.header").as("header")
            .column("a.summary").as("summary")
            .column("a.status").as("status")
            .column("a.last_updated").as("last_updated")
            .column("ath.firstname").as("firstname")
            .column("ath.lastname").as("lastname")
            .column("ath.username").as("username")
            .count("v.id").as("views")
            .count("l.article_id").as("likes")
            .from("Article a")
            .join("Views v", "v.article_id = a.id")
            .join("Likes l", "l.article_id = a.id")
            .join("UserAccount ath", "ath.id = a.author_id")
            .where("a.search_document @@ to_tsquery('english', ?)")
            .and("a.status = 'PUBLISHED'");

    static final String PAGE_OF_ARTICLES_BASED_ON_HISTORY = withAndSelect(
            "recent_articles", select()
                    .column("a.header").as("header")
                    .column("a.summary").as("summary")
                    .column("a.body").as("body")
                    .from("Article a")
                    .join("Views v", "v.article_id = a.id")
                    .join("UserAccount u", "u.username = ?")
                    .where("v.reader_id = u.id")
                    .orderBy("v.creation_date", Order.DESC)
                    .limitAndOffset(12, 0)
                    .toSQlQuery())
            .column("a.id").as("id")
            .column("a.author_id").as("author_id")
            .column("a.header").as("header")
            .column("a.summary").as("summary")
            .column("a.status").as("status")
            .column("a.last_updated").as("last_updated")
            .column("ath.firstname").as("firstname")
            .column("ath.lastname").as("lastname")
            .column("ath.username").as("username")
            .count("v.id").as("views")
            .count("l.article_id").as("likes")
            .from("Article a")
            .join("Views v", "v.article_id = a.id")
            .join("Likes l", "l.article_id = a.id")
            .join("UserAccount ath", "ath.id = a.author_id")
            .where("""
                    a.search_document @@ to_tsquery('english',
                          (SELECT string_agg(header, ' & ') FROM recent_articles) ||
                          ' & ' ||
                          (SELECT string_agg(summary, ' & ') FROM recent_articles) ||
                          ' & ' ||
                          (SELECT string_agg(body, ' & ') FROM recent_articles))
                    """)
            .and("a.status = 'PUBLISHED")
            .limitAndOffset()
            .sql();

    static final String ARCHIVE = select()
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
            .join("UserAccount u", "u.id = a.author_id")
            .where("u.username = ?")
            .and("a.status = 'ARCHIVED'")
            .limitAndOffset()
            .sql();

    static final String DRAFT = select()
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
            .join("UserAccount u", "u.id = a.author_id")
            .where("u.username = ?")
            .and("a.status = 'DRAFT'")
            .limitAndOffset()
            .sql();

    JdbcOutboundArticleRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public boolean isViewExists(UUID articleID, UUID userID) {
        return jet.readObjectOf(VIEW, Integer.class, articleID.toString(), userID.toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking view existence.");
                    return false;
                });
    }

    @Override
    public boolean isArticleExists(UUID articleID) {
        return jet.readObjectOf(IS_ARTICLE_EXISTS, Integer.class, articleID.toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking article existence.");
                    return false;
                });
    }

    @Override
    public Result<List<ArticlePreview>, Throwable> page(ArticlesQueryForm query) {
        int limit = buildLimit(query.pageSize());
        int offSet = buildOffSet(limit, query.pageSize());

        String sql = buildQuery(query);
        if (Objects.nonNull(query.authorName()) && Objects.nonNull(query.tag())) {
            var articlesList = jet.readListOf(sql,
                    this::articlePreviewMapper,
                    query.searchQuery(),
                    query.authorName(),
                    query.tag(),
                    query.sortBy().toString(),
                    limit,
                    offSet
            );
            return new Result<>(articlesList.value(), articlesList.throwable(), articlesList.success());
        }
        if (Objects.isNull(query.authorName()) && Objects.nonNull(query.tag())) {
            var articlesList = jet.readListOf(sql,
                    this::articlePreviewMapper,
                    query.searchQuery(),
                    query.tag(),
                    query.sortBy().toString(),
                    limit,
                    offSet
            );
            return new Result<>(articlesList.value(), articlesList.throwable(), articlesList.success());
        }
        if (Objects.nonNull(query.authorName())) {
            var articlesList = jet.readListOf(sql,
                    this::articlePreviewMapper,
                    query.searchQuery(),
                    query.authorName(),
                    query.sortBy().toString(),
                    limit,
                    offSet
            );
            return new Result<>(articlesList.value(), articlesList.throwable(), articlesList.success());
        }
        var articlesList = jet.readListOf(sql,
                this::articlePreviewMapper,
                query.searchQuery(),
                query.sortBy().toString(),
                limit,
                offSet
        );
        return new Result<>(articlesList.value(), articlesList.throwable(), articlesList.success());
    }

    @Override
    public Result<List<ArticlePreview>, Throwable> page(int pageNumber, int pageSize, Username username) {
        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);

        Integer countOfViews = jet.readObjectOf(USER_VIEWS_COUNT, Integer.class, username.username())
                .orElseThrow(() -> new IllegalStateException("Can`t find user views."));

        if (countOfViews == 0) {
            var articlesList = jet.readListOf(ARTICLES, this::articlePreviewMapper, username.username(), limit, offSet);
            return new Result<>(articlesList.value(), articlesList.throwable(), articlesList.success());
        }

        var articlesList = jet.readListOf(PAGE_OF_ARTICLES_BASED_ON_HISTORY,
                this::articlePreviewMapper,
                username.username(), limit, offSet);
        return new Result<>(articlesList.value(), articlesList.throwable(), articlesList.success());
    }

    @Override
    public Result<List<Article>, Throwable> archive(int pageNumber, int pageSize, Username username) {
        return listOfArticles(pageNumber, pageSize, username.username(), ARCHIVE);
    }

    @Override
    public Result<List<Article>, Throwable> draft(int pageNumber, int pageSize, Username username) {
        return listOfArticles(pageNumber, pageSize, username.username(), DRAFT);
    }

    @Override
    public Result<Article, Throwable> article(UUID articleID) {
        var tagsResult = jet.readListOf(ARTICLE_TAGS, this::articleTagsMapper, articleID.toString());
        if (!tagsResult.success()) return Result.failure(tagsResult.throwable());
        var articleTags = new Result<>(tagsResult.value(), tagsResult.throwable(), true);

        var articleResult = jet.read(ARTICLE, this::articleMapper, articleID.toString());
        if (!articleResult.success()) return Result.failure(articleResult.throwable());
        var article = new Result<>(articleResult.value(), articleResult.throwable(), true);

        articleTags.value().forEach(articleTag -> article.value().addTag(articleTag));
        return article;
    }

    private ArticleTag articleTagsMapper(ResultSet rs) throws SQLException {
        return new ArticleTag(rs.getString("tag"));
    }

    private Result<List<Article>, Throwable> listOfArticles(int pageNumber, int pageSize, String username, String sql) {
        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);

        var articlesResult = jet.readListOf(sql, this::articleMapper, username, limit, offSet);
        if (!articlesResult.success()) return Result.failure(articlesResult.throwable());
        return new Result<>(articlesResult.value(), articlesResult.throwable(), true);
    }

    private Article articleMapper(ResultSet rs) throws SQLException {
        ArticleDates events = new ArticleDates(
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

    private ArticlePreview articlePreviewMapper(ResultSet rs) throws SQLException {
        return new ArticlePreview(
                rs.getString("id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getString("username"),
                rs.getString("header"),
                rs.getString("summary"),
                rs.getString("status"),
                rs.getLong("views"),
                rs.getLong("likes"),
                rs.getObject("last_updated", Timestamp.class).toLocalDateTime()
        );
    }

    static int buildLimit(Integer pageSize) {
        int limit;
        if (pageSize > 0 && pageSize <= 25) {
            limit = pageSize;
        } else {
            limit = 10;
        }
        return limit;
    }

    static int buildOffSet(Integer limit, Integer pageNumber) {
        int offSet;
        if (limit > 0 && pageNumber > 0) {
            offSet = (pageNumber - 1) * limit;
        } else {
            offSet = 0;
        }
        return offSet;
    }

    static String buildQuery(ArticlesQueryForm query) {
        ChainedWhereBuilder sql = PAGE_OF_ARTICLES;
        if (query.authorName() != null) sql.and("a.author_id = (SELECT id FROM UserAccount WHERE username = ?)");
        if (query.tag() != null) sql.and("EXISTS (SELECT 1 FROM ArticleTags at WHERE at.article_id = a.id AND at.tag = ?)");

        switch (query.sortBy()) {
            case VIEWS_ASC -> sql.orderBy("views ASC");
            case VIEWS_DESC -> sql.orderBy("views DESC");
            case LIKES_ASC -> sql.orderBy("likes ASC");
            case LIKES_DESC -> sql.orderBy("likes DESC");
            case LAST_MODIFICATION_ASC -> sql.orderBy("a.last_updated ASC");
            case LAST_MODIFICATION_DESC -> sql.orderBy("a.last_updated DESC");
        }
        return sql.limitAndOffset().sql();
    }
}
