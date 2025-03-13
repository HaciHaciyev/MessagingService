package core.project.messaging.infrastructure.events;

import core.project.messaging.domain.articles.events.*;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuarkusArticlesEventsPublisher implements ArticlesEventsPublisher {

    @Inject
    EventBus eventBus;

    @Override
    public void publish(ArticleUpdatedEvent articleEvent) {
        eventBus.publish("articles", articleEvent);
    }

    @Override
    public void publish(CommentEditedEvent commentEvent) {
        eventBus.publish("comments", commentEvent);
    }
}
