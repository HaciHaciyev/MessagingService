package core.project.messaging.domain.articles.events;

import java.time.LocalDateTime;

public record CommentEvents(LocalDateTime creationDate, LocalDateTime lastUpdatedDate) {}
