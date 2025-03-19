package core.project.messaging.application.dto;

public record ArticlePreview(String id,
                             String header,
                             String summary,
                             long views,
                             long likes) {
}
