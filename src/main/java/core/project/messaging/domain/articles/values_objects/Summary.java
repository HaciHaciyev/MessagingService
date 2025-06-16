package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Summary(String value) {

    public static final int MAX_SIZE = 256;

    public static final String INVALID_CHARACTERS_REGEX = "[\\\\p{C}]";

    public static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile(INVALID_CHARACTERS_REGEX);

    public Summary {
        if (value == null) {
            throw new IllegalDomainArgumentException("Summary must not be null.");
        }
        if (value.isBlank()) {
            throw new IllegalDomainArgumentException("Summary must not be blank.");
        }
        if (value.length() > MAX_SIZE) {
            throw new IllegalDomainArgumentException("Summary must not exceed %d characters.".formatted(MAX_SIZE));
        }

        Matcher matcher = INVALID_CHARACTERS_PATTERN.matcher(value);
        if (matcher.matches()) {
            throw new IllegalDomainArgumentException("Header contains invalid characters.");
        }
    }
}
