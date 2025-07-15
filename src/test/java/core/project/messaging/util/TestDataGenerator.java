package core.project.messaging.util;

import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.value_objects.*;
import jakarta.enterprise.context.ApplicationScoped;
import net.datafaker.Faker;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class TestDataGenerator {

    static Faker faker = new Faker();

    static ThreadLocalRandom random = ThreadLocalRandom.current();

    public static User user(Username username, boolean isVerified) {
        return new User(
                UUID.randomUUID(),
                generateFirstname(),
                generateSurname(),
                username,
                generateEmail(),
                generatePassword(),
                isVerified,
                Rating.defaultRating(),
                AccountDates.defaultEvents()
        );
    }

    public static User unverifiedUser() {
        return new User(
                UUID.randomUUID(),
                generateFirstname(),
                generateSurname(),
                generateUsername(),
                generateEmail(),
                generatePassword(),
                false,
                Rating.defaultRating(),
                AccountDates.defaultEvents()
        );
    }

    public static User verifiedUser() {
        return new User(
                UUID.randomUUID(),
                generateFirstname(),
                generateSurname(),
                generateUsername(),
                generateEmail(),
                generatePassword(),
                true,
                Rating.defaultRating(),
                AccountDates.defaultEvents()
        );
    }

    public static Username generateUsername() {
        while (true) {
            var usernameResult = Result.ofThrowable(() -> new Username(faker.name().firstName()));
            if (!usernameResult.success()) {
                continue;
            }
            return usernameResult.value();
        }
    }

    public static Firstname generateFirstname() {
        while (true) {
            var firstnameResult = Result.ofThrowable(() -> new Firstname(faker.name().firstName()));
            if (!firstnameResult.success()) continue;
            return firstnameResult.value();
        }
    }

    public static Surname generateSurname() {
        while (true) {
            var surnameResult = Result.ofThrowable(() -> new Surname(faker.name().lastName()));
            if (!surnameResult.success()) continue;
            return surnameResult.value();
        }
    }

    public static Email generateEmail() {
        while (true) {
            var emailResult = Result.ofThrowable(() -> new Email(faker.internet().emailAddress()));
            if (!emailResult.success()) continue;
            return emailResult.value();
        }
    }

    public static Password generatePassword() {
        while (true) {
            var passwordResult = Result.ofThrowable(() -> new Password(faker.internet().password()));
            if (!passwordResult.success()) continue;
            return passwordResult.value();
        }
    }
}
