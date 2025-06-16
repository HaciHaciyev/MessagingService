package core.project.messaging.application.dto.articles;

import core.project.messaging.domain.commons.annotations.Nullable;

public record CommentForm(String articleID,
                          String text,
                          @Nullable String parentCommentID,
                          @Nullable String respondTo) {}
