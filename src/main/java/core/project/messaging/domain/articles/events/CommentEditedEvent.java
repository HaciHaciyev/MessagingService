package core.project.messaging.domain.articles.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommentEditedEvent {
    private final UUID commentID;
    private final LocalDateTime data;

    public CommentEditedEvent(UUID commentID) {
        this.commentID = commentID;
        this.data = LocalDateTime.now();
    }

    public UUID commentID() {
        return commentID;
    }

    public LocalDateTime data() {
        return data;
    }
}
