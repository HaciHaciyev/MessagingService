package core.project.messaging.application.dto;

import jakarta.annotation.Nullable;

public record CommentForm(String articleID,
                          String text,
                          @Nullable String parentCommentID,
                          @Nullable String respondTo) {

    public CommentForm {
        if (articleID == null || text == null) {
            throw new IllegalArgumentException("ArticleID or text cannot be null");
        }
    }
}
