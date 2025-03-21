package core.project.messaging.domain.articles.values_objects;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLike(UUID commentId, UUID userId, LocalDateTime likedAt) {}
