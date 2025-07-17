package core.project.messaging.util;

import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.enumerations.CommentType;
import core.project.messaging.domain.articles.values_objects.*;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.value_objects.*;
import jakarta.enterprise.context.ApplicationScoped;
import net.datafaker.Faker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class TestDataGenerator {

    static Faker faker = new Faker();

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

    public static Comment parentTypeComment() {
        return Comment.of(
                new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                generateCommentText(),
                new Reference(CommentType.PARENT, null, null)
        );
    }

    public static Comment childTypeComment(UUID parentCommentID, UUID respondTo) {
        return Comment.of(
                new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                generateCommentText(),
                new Reference(CommentType.PARENT, parentCommentID, respondTo)
        );
    }

    public static Article article(ArticleStatus status) {
        return Article.of(
                UUID.randomUUID(),
                generateTags(),
                generateHeader(),
                generateSummary(),
                generateBody(),
                status
        );
    }

    public static Article article(ArticleStatus status, int tagsCount) {
        return Article.of(
                UUID.randomUUID(),
                generateTags(tagsCount),
                generateHeader(),
                generateSummary(),
                generateBody(),
                status
        );
    }

    public static Body generateBody() {
        while (true) {
            var bodyResult = Result.ofThrowable(() -> new Body(faker.lorem().characters()));
            if (!bodyResult.success()) continue;
            return bodyResult.value();
        }
    }

    public static Summary generateSummary() {
        while (true) {
            var summaryResult = Result.ofThrowable(() -> new Summary(faker.lorem().characters()));
            if (!summaryResult.success()) continue;
            return summaryResult.value();
        }
    }

    public static Header generateHeader() {
        while (true) {
            var headerResult = Result.ofThrowable(() -> new Header(faker.lorem().characters(1, 128)));
            if (!headerResult.success()) continue;
            return headerResult.value();
        }
    }

    public static Set<ArticleTag> generateTags() {
        Set<ArticleTag> tags = new HashSet<>();
        int count = ThreadLocalRandom.current().nextInt(4, 7);

        for (int i = 0; i < count; i++) {
            String tagName = faker.book().genre();
            tags.add(new ArticleTag(tagName));
        }

        return tags;
    }

    public static ArticleTag generateTag() {
        return new ArticleTag(faker.book().genre());
    }

    public static Set<ArticleTag> generateTags(int count) {
        Set<ArticleTag> tags = new HashSet<>();

        for (int i = 0; i < count; i++) {
            String tagName = faker.book().genre();
            tags.add(new ArticleTag(tagName));
        }

        return tags;
    }

    public static CommentText generateCommentText() {
        while (true) {
            var commentTextResult = Result.ofThrowable(() -> new CommentText(faker.lorem().characters(3, 56)));
            if (!commentTextResult.success()) continue;
            return commentTextResult.value();
        }
    }

    public static Username generateUsername() {
        while (true) {
            var usernameResult = Result.ofThrowable(() -> new Username(faker.name().firstName()));
            if (!usernameResult.success()) continue;
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
