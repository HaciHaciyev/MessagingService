package core.project.messaging.domain.articles.events;

public interface ArticlesEventsPublisher {

    void publish(ArticleEvents articleEvent);

    void publish(CommentEvents commentEvents);
}
