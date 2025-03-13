package core.project.messaging.application.dto;

import jakarta.annotation.Nullable;

public record ArticleText(@Nullable String header,
                          @Nullable String summary,
                          @Nullable String body) {}
