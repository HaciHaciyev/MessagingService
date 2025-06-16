package core.project.messaging.domain.user.value_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Firstname(String firstname) {

    public static final int MIN_SIZE = 2;

    public static final int MAX_SIZE = 48;

    private static final String FIRSTNAME_REGEX = "^[A-Za-z]+$";

    private static final Pattern FIRST_NAME_PATTERN = Pattern.compile(FIRSTNAME_REGEX);

    public Firstname {
        validate(firstname);
    }

    public static void validate(String firstname) {
        if (firstname == null) {
            throw new IllegalDomainArgumentException("Firstname must not be null.");
        }
        if (firstname.isBlank()) {
            throw new IllegalDomainArgumentException("First Name should`t be blank.");
        }
        if (firstname.length() < MIN_SIZE || firstname.length() > MAX_SIZE) {
            throw new IllegalDomainArgumentException("Fist Name should`t be smaller than 3 characters and greater than 25.");
        }

        Matcher matcher = FIRST_NAME_PATTERN.matcher(firstname);
        if (!matcher.matches()) {
            throw new IllegalDomainArgumentException("First Name should match regex.");
        }
    }
}
