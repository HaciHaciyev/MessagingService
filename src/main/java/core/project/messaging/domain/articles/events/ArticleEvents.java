package core.project.messaging.domain.articles.events;

import java.time.LocalDateTime;
import java.util.Objects;

public record ArticleEvents(LocalDateTime creationDate, LocalDateTime lastUpdateDate) {

    public ArticleEvents {
        Objects.requireNonNull(creationDate);
        Objects.requireNonNull(lastUpdateDate);
    }

    public ArticleEvents defaultEvents() {
        return new ArticleEvents(LocalDateTime.now(), LocalDateTime.now());
    }
}
