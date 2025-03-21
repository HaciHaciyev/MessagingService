package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.user.value_objects.Username;
import jakarta.annotation.Nullable;

import java.util.Objects;

import static core.project.messaging.domain.articles.services.ArticlesService.SEARCH_QUERY_MAX_SIZE;
import static core.project.messaging.domain.articles.services.ArticlesService.SEARCH_QUERY_MIN_SIZE;

public record ArticlesQueryForm(String searchQuery,
                                @Nullable String authorName,
                                @Nullable String tag,
                                @Nullable SortBy sortBy,
                                int pageNumber,
                                int pageSize) {

    public ArticlesQueryForm {
        if (searchQuery == null) {
            throw new IllegalArgumentException("Search query can`t be null.");
        }
        sortBy = Objects.requireNonNullElse(sortBy, SortBy.VIEWS_DESC);

        validateSearchQuery(searchQuery);

        if (Objects.nonNull(authorName) && !Username.validate(authorName)) {
            throw new IllegalArgumentException("Author name is invalid.");
        }

        if (Objects.nonNull(tag)) {
            ArticleTag.validate(tag);
        }
    }

    public enum SortBy {
        VIEWS_ASC,
        VIEWS_DESC,
        LIKES_ASC,
        LIKES_DESC,
        LAST_MODIFICATION_ASC,
        LAST_MODIFICATION_DESC
    }

    public static void validateQuery(ArticlesQueryForm query) {
        String search = query.searchQuery();
        validateSearchQuery(search);

        if (Objects.nonNull(query.authorName()) && !Username.validate(query.authorName())) {
            throw new IllegalArgumentException("Author name is invalid.");
        }

        if (Objects.nonNull(query.tag())) {
            ArticleTag.validate(query.tag());
        }
    }

    static void validateSearchQuery(String search) {
        if (search.isBlank()) {
            throw new IllegalArgumentException("Search is blank");
        }
        if (search.length() < SEARCH_QUERY_MIN_SIZE) {
            throw new IllegalArgumentException("Search is too short: min size 3 characters");
        }
        if (search.length() > SEARCH_QUERY_MAX_SIZE) {
            throw new IllegalArgumentException("Search is too long: max size 64 characters");
        }
    }
}

