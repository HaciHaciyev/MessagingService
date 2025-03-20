package core.project.messaging.domain.articles.values_objects;

public record ArticleTag(String value) {

    public static final int MAX_SIZE = 24;

    public ArticleTag {
        if (value == null) {
            throw new IllegalArgumentException("ArticleTag cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be blank.");
        }
        if (value.length() > MAX_SIZE) {
            throw new IllegalArgumentException("Tag must be less than 56 characters.");
        }
    }

    public static void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ArticleTag cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be blank.");
        }
        if (value.length() > MAX_SIZE) {
            throw new IllegalArgumentException("Tag must be less than 56 characters.");
        }
    }
}