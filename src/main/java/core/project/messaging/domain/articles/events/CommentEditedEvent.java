package core.project.messaging.domain.articles.events;

import java.time.LocalDateTime;

public class CommentEditedEvent {

    private final LocalDateTime data;

    public CommentEditedEvent() {
        this.data = LocalDateTime.now();
    }
}
