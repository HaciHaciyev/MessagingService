package core.project.messaging.application.dto.articles;

import jakarta.annotation.Nullable;

public record CommentForm(String articleID,
                          String text,
                          @Nullable String parentCommentID,
                          @Nullable String respondTo) {}
