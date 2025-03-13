package core.project.messaging.infrastructure.events;

import core.project.messaging.domain.articles.events.ArticleEvents;
import core.project.messaging.domain.articles.events.ArticlesEventsPublisher;
import core.project.messaging.domain.articles.events.CommentEvents;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuarkusArticlesEventsPublisher implements ArticlesEventsPublisher {

    @Inject
    EventBus eventBus;

    @Override
    public void publish(ArticleEvents articleEvent) {
        eventBus.publish("articles", articleEvent);
    }

    @Override
    public void publish(CommentEvents commentEvent) {
        eventBus.publish("comments", commentEvent);
    }
}
