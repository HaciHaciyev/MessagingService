package core.project.messaging.application.dto;

import java.time.LocalDateTime;

public record ArticlePreview(String id,
                             String authorFirstName,
                             String authorLastName,
                             String authorUsername,
                             String header,
                             String summary,
                             String status,
                             long views,
                             long likes,
                             LocalDateTime lastUpdated) {
}
