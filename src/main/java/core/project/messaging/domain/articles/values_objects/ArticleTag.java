package core.project.messaging.domain.articles.values_objects;

import java.util.Objects;

public record ArticleTag(String value) {

    public ArticleTag {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be blank.");
        }
        if (value.length() > 56) {
            throw new IllegalArgumentException("Tag must be less than 56 characters.");
        }
    }
}