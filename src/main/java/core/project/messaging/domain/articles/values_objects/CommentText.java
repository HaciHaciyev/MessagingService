package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CommentText(String value) {

    public static final String DELETED_COMMENT = "Deleted comment.";

    public static final String INVALID_CHARACTERS_REGEX = "[\\\\p{C}]";

    public static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile(INVALID_CHARACTERS_REGEX);

    public CommentText {
        if (value == null) {
            throw new IllegalDomainArgumentException("Comment value is null");
        }
        if (value.isBlank()) {
            throw new IllegalDomainArgumentException("Comment text cannot be blank.");
        }
        if (value.length() < 3) {
            throw new IllegalDomainArgumentException("");
        }
        if (value.length() > 56) {
            throw new IllegalDomainArgumentException("Comment text cannot be longer than 56 characters.");
        }

        Matcher matcher = INVALID_CHARACTERS_PATTERN.matcher(value);
        if (matcher.matches()) {
            throw new IllegalDomainArgumentException("Content contains invalid characters.");
        }
    }
}
