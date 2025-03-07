package core.project.messaging.domain.articles.values_objects;

import java.util.Objects;

public record ArticleTag(String value) {

    public static final int MAX_SIZE = 24;

    public ArticleTag {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be blank.");
        }
        if (value.length() > MAX_SIZE) {
            throw new IllegalArgumentException("Tag must be less than 56 characters.");
        }
    }
}