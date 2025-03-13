package core.project.messaging.infrastructure.events;

import core.project.messaging.domain.articles.events.ArticleEvents;
import core.project.messaging.domain.articles.events.CommentEvents;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArticlesEventsHandler {

    @ConsumeEvent("articles")
    public void consumeArticleEvent(ArticleEvents articleEvent) {

    }

    @ConsumeEvent("comments")
    public void consumeCommentEvent(CommentEvents commentEvent) {

    }
}
