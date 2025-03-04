package core.project.messaging.domain.articles.values_objects;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Content(String content) {

    public static final int MAX_SIZE = 5120;

    public static final String INVALID_CHARACTERS_REGEX = "[\\\\p{C}]";

    public static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile(INVALID_CHARACTERS_REGEX);

    public Content {
        Objects.requireNonNull(content, "Content must not be null.");
        if (content.isBlank()) {
            throw new IllegalArgumentException("Content is blank.");
        }
        if (content.length() > MAX_SIZE) {
            throw new IllegalArgumentException("Content is too long: max size %d characters.".formatted(MAX_SIZE));
        }

        Matcher matcher = INVALID_CHARACTERS_PATTERN.matcher(content);
        if (matcher.matches()) {
            throw new IllegalArgumentException("Content contains invalid characters.");
        }
    }
}
