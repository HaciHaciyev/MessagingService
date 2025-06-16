package core.project.messaging.domain.articles.values_objects;

import java.time.LocalDateTime;

public record CommentDates(LocalDateTime creationDate, LocalDateTime lastUpdatedDate) {}
