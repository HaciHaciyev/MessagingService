package core.project.messaging.domain.user.value_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Username(String username) {

    private static final String usernameRegex = "^[a-zA-Z0-9]*$";

    private static final Pattern pattern = Pattern.compile(usernameRegex);

    public Username {
        if (Objects.isNull(username)) {
            throw new IllegalDomainArgumentException("Username cannot be null");
        }
        if (username.isBlank()) {
            throw new IllegalDomainArgumentException("Username cannot be blank");
        }

        Matcher matcher = pattern.matcher(username);
        if (!matcher.matches()) {
            throw new IllegalDomainArgumentException("Username contains invalid characters");
        }
    }

    public static boolean validate(String username) {
        if (Objects.isNull(username)) {
            return false;
        }
        if (username.isBlank()) {
            return false;
        }

        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
}