package core.project.messaging.domain.articles.events;

public interface ArticlesEventsPublisher {

    void publish(ArticleUpdatedEvent articleEvent);

    void publish(CommentEditedEvent commentEvents);
}
