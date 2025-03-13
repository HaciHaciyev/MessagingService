package core.project.messaging.domain.articles.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ArticleUpdatedEvent {
    private final UUID articleID;
    private final LocalDateTime data;

    public ArticleUpdatedEvent(UUID articleID) {
        this.articleID = articleID;
        this.data = LocalDateTime.now();
    }

    public LocalDateTime data() {
        return data;
    }

    public UUID articleID() {
        return articleID;
    }
}
