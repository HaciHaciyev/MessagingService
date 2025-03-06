package core.project.messaging.domain.articles.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public record View(UUID id, UUID articleID, UUID readerID, LocalDateTime viewedData) {}
