package core.project.messaging.domain.articles.values_objects;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Summary(String summary) {

    public static final int MAX_SIZE = 256;

    public static final String INVALID_CHARACTERS_REGEX = "[\\\\p{C}]";

    public static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile(INVALID_CHARACTERS_REGEX);

    public Summary {
        Objects.requireNonNull(summary, "Summary must not be null.");
        if (summary.isBlank()) {
            throw new IllegalArgumentException("Summary must not be blank.");
        }
        if (summary.length() > MAX_SIZE) {
            throw new IllegalArgumentException("Summary must not exceed %d characters.".formatted(MAX_SIZE));
        }

        Matcher matcher = INVALID_CHARACTERS_PATTERN.matcher(summary);
        if (matcher.matches()) {
            throw new IllegalArgumentException("Header contains invalid characters.");
        }
    }
}
