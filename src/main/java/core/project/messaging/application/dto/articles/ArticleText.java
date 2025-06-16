package core.project.messaging.application.dto.articles;

import core.project.messaging.domain.commons.annotations.Nullable;

public record ArticleText(@Nullable String header,
                          @Nullable String summary,
                          @Nullable String body) {}
