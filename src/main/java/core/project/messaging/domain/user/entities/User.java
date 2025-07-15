package core.project.messaging.domain.user.entities;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;
import core.project.messaging.domain.commons.exceptions.IllegalDomainStateException;
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
    private final Set<UUID> partners;

    public User(UUID id, Firstname firstname, Surname surname, Username username, Email email,
                Password password, boolean isEnable, Rating rating, AccountDates accountDates) {
        if (id == null) throw new IllegalDomainArgumentException("Id can't be null.");
        if (firstname == null) throw new IllegalDomainArgumentException("Firstname can't be null.");
        if (surname == null) throw new IllegalDomainArgumentException("Surname can't be null.");
        if (username == null) throw new IllegalDomainArgumentException("Username can't be null.");
        if (email == null) throw new IllegalDomainArgumentException("Email can't be null.");
        if (password == null) throw new IllegalDomainArgumentException("Password can't be null.");
        if (rating == null) throw new IllegalDomainArgumentException("Rating can't be null.");
        if (accountDates == null) throw new IllegalDomainArgumentException("AccountDates can't be null.");

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

    public Rating rating() {
        return this.rating;
    }

    public Set<UUID> partners() {
        return new HashSet<>(partners);
    }

    public boolean containsPartner(final User user) {
        return partners.contains(user.id());
    }

    public void addPartner(final User partner) {
        if (partner == null) throw new IllegalDomainArgumentException("Partner can`t be null.");
        if (!isEnable) throw new IllegalDomainStateException("Can`t add partner to unverified account.");
        if (!partner.isEnable) throw new IllegalDomainStateException("Partner account is not verified.");
        if (partner.username.equals(this.username)) return;

        this.partners.add(partner.id());
        if (!partner.containsPartner(this)) partner.addPartner(this);
    }

    public void removePartner(final User partner) {
        if (partner == null) throw new IllegalDomainArgumentException("Partner can`t be null.");
        if (!isEnable) throw new IllegalDomainStateException("Can`t remove partner from unverified account.");
        partners.remove(partner.id());
        if (partner.containsPartner(this)) partner.removePartner(this);
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