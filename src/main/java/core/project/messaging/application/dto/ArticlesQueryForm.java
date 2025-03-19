package core.project.messaging.application.dto;

import jakarta.annotation.Nullable;

public record ArticlesQueryForm(int pageNumber,
                                int pageSize,
                                @Nullable String authorName,
                                @Nullable String tag,
                                @Nullable String searchQuery,
                                @Nullable SortBy sortBy) {

    public enum SortBy {
        VIEWS_ASC,
        VIEWS_DESC,
        LIKES_ASC,
        LIKES_DESC,
        LAST_MODIFICATION_ASC,
        LAST_MODIFICATION_DESC
    }
}
