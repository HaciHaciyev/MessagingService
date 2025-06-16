package core.project.messaging.domain.user.value_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.Objects;

public record Password(String password) {
    public static final int MIN_SIZE = 8;
    public static final int MAX_SIZE = 64;

    public Password {
        if (Objects.isNull(password)) {
            throw new IllegalDomainArgumentException("Password cannot be null");
        }
        if (password.isBlank()) {
            throw new IllegalDomainArgumentException("Password cannot be blank");
        }
        if (password.length() < MIN_SIZE) {
            throw new IllegalDomainArgumentException("Password must be at least 8 characters");
        }
    }

    public static boolean validateMaxSize(String password) {
        return password.length() <= MAX_SIZE;
    }
}
