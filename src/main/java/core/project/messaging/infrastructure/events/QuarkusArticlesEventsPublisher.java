package core.project.messaging.infrastructure.events;

import core.project.messaging.domain.articles.events.*;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuarkusArticlesEventsPublisher implements ArticlesEventsPublisher {

    private final EventBus eventBus;

    QuarkusArticlesEventsPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void publish(ArticleUpdatedEvent articleEvent) {
        eventBus.publish("articles", articleEvent);
    }

    @Override
    public void publish(CommentEditedEvent commentEvent) {
        eventBus.publish("comments", commentEvent);
    }
}
