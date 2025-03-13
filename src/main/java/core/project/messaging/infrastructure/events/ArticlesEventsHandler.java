package core.project.messaging.infrastructure.events;

import core.project.messaging.domain.articles.events.ArticleUpdatedEvent;
import core.project.messaging.domain.articles.events.CommentEditedEvent;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.domain.articles.repositories.InboundCommentRepository;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArticlesEventsHandler {

    private final InboundArticleRepository inboundArticleRepository;

    private final InboundCommentRepository inboundCommentRepository;

    ArticlesEventsHandler(InboundArticleRepository inboundArticleRepository, InboundCommentRepository inboundCommentRepository) {
        this.inboundArticleRepository = inboundArticleRepository;
        this.inboundCommentRepository = inboundCommentRepository;
    }

    @ConsumeEvent("articles")
    public void consumeArticleEvent(ArticleUpdatedEvent articleEvent) {
        inboundArticleRepository.updateEvent(articleEvent);
    }

    @ConsumeEvent("comments")
    public void consumeCommentEvent(CommentEditedEvent commentEvent) {
        inboundCommentRepository.updateEvent(commentEvent);
    }
}
