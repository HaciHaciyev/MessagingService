package core.project.messaging.domain.articles.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public record Like(UUID id, UUID articleID, UUID userId, LocalDateTime likedAt) {}
