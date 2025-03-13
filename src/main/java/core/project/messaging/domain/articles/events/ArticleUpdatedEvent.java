package core.project.messaging.domain.articles.events;

import java.time.LocalDateTime;

public class ArticleUpdatedEvent {

    private final LocalDateTime data;

    public ArticleUpdatedEvent() {
        this.data = LocalDateTime.now();
    }

    public LocalDateTime data() {
        return data;
    }
}
