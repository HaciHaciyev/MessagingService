package core.project.messaging.domain.user.entities;

import core.project.messaging.domain.user.value_objects.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User {
    private final UUID id;
    private final Firstname firstname;
    private final Surname surname;
    private final Username username;
    private final Email email;
    private final Password password;
    private boolean isEnable;
    private Rating rating;
    private final AccountDates accountDates;
    private final Set<User> partners;

    public User(UUID id, Firstname firstname, Surname surname, Username username, Email email,
                Password password, boolean isEnable, Rating rating, AccountDates accountDates) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(firstname);
        Objects.requireNonNull(surname);
        Objects.requireNonNull(username);
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        Objects.requireNonNull(rating);
        Objects.requireNonNull(accountDates);

        this.id = id;
        this.firstname = firstname;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isEnable = isEnable;
        this.rating = rating;
        this.accountDates = accountDates;
        this.partners = new HashSet<>();
    }

    public UUID id() {
        return id;
    }

    public Firstname firstname() {
        return firstname;
    }

    public Surname surname() {
        return surname;
    }

    public Username username() {
        return username;
    }

    public Email email() {
        return email;
    }

    public Password password() {
        return password;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public AccountDates accountEvents() {
        return accountDates;
    }

    public boolean isEnabled() {
        return isEnable;
    }

    public Rating rating() {
        return this.rating;
    }

    public Set<User> partners() {
        return new HashSet<>(partners);
    }

    public boolean containsPartner(final User user) {
        return partners.contains(user);
    }

    public void addPartner(final User partner) {
        Objects.requireNonNull(partner);
        if (partner.username.equals(this.username)) return;

        this.partners.add(partner);
        if (!partner.containsPartner(this)) partner.addPartner(this);
    }

    public void removePartner(final User partner) {
        Objects.requireNonNull(partner);
        partners.remove(partner);
        partner.removePartner(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;

        return isEnable == that.isEnable && Objects.equals(id, that.id) && Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) && Objects.equals(password, that.password) &&
                Objects.equals(rating, that.rating) && Objects.equals(accountDates, that.accountDates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, username, email, password, isEnable, rating, accountDates
        );
    }

    @Override
    public String toString() {
        String enables;
        if (Boolean.TRUE.equals(isEnable)) {
            enables = "enable";
        } else {
            enables = "disable";
        }

        return String.format("""
               UserAccount: %s {
                    Username : %s,
                    Email : %s,
                    Is enable : %s,
                    Rating : %f,
                    Creation date : %s,
                    Last updated date : %s
               }
               """,
                id,
                username.username(),
                email.email(),
                enables,
                rating.rating(),
                accountDates.creationDate().toString(),
                accountDates.lastUpdateDate().toString()
        );
    }
}