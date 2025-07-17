package core.project.messaging.domain.articles.values_objects;

import java.time.LocalDateTime;

public record CommentDates(LocalDateTime creationDate, LocalDateTime lastUpdatedDate) {

    public static CommentDates defaultDates() {
        return new CommentDates(LocalDateTime.now(), LocalDateTime.now());
    }
}
