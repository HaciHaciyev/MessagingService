package core.project.messaging.infrastructure.dal.repository.inbound;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.insert;

@Transactional
@ApplicationScoped
public class JdbcInboundArticleRepository implements InboundArticleRepository {

    private final JDBC jdbc;

    static final String SAVE_ARTICLE = insert()
            .into("Articles")
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
                article.status(),
                article.events().creationDate(),
                article.events().lastUpdateDate()
        );

        article.tags().forEach(articleTag -> jdbc.write(SAVE_ARTICLE_TAGS, articleTag, articleID, articleTag));
    }
}
