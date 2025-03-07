package core.project.messaging.domain.articles.values_objects;

import java.time.LocalDateTime;
import java.util.UUID;

public record Like(UUID articleID, UUID userId, LocalDateTime likedAt) {}
