package core.project.messaging.domain.user.entities;

import core.project.messaging.domain.user.enumerations.UserRole;
import core.project.messaging.domain.user.events.AccountEvents;
import core.project.messaging.domain.user.value_objects.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class UserAccount {
    private final UUID id;
    private final Firstname firstname;
    private final Surname surname;
    private final Username username;
    private final Email email;
    private final Password password;
    private UserRole userRole;
    private boolean isEnable;
    private Rating rating;
    private final AccountEvents accountEvents;
    private final Set<UserAccount> partners;

    public UserAccount(UUID id, Firstname firstname, Surname surname, Username username, Email email, Password password,
                       UserRole userRole, boolean isEnable, Rating rating, AccountEvents accountEvents, Set<UserAccount> partners) {
        this.id = id;
        this.firstname = firstname;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.isEnable = isEnable;
        this.rating = rating;
        this.accountEvents = accountEvents;
        this.partners = partners;
    }

    /**
     * this method is used to call only from repository
     */
    public static UserAccount fromRepository(UUID id, Firstname firstname, Surname surname, Username username, Email email,
                                             Password password, UserRole userRole, boolean enabled, Rating rating, AccountEvents events) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(firstname);
        Objects.requireNonNull(surname);
        Objects.requireNonNull(username);
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        Objects.requireNonNull(userRole);
        Objects.requireNonNull(rating);
        Objects.requireNonNull(events);

        return new UserAccount(id, firstname, surname, username, email, password, userRole, enabled, rating, events, new HashSet<>());
    }

    public UUID getId() {
        return id;
    }

    public Firstname getFirstname() {
        return firstname;
    }

    public Surname getSurname() {
        return surname;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public AccountEvents getAccountEvents() {
        return accountEvents;
    }

    public boolean isEnabled() {
        return isEnable;
    }

    public Rating getRating() {
        return this.rating;
    }

    public Set<UserAccount> getPartners() {
        return new HashSet<>(partners);
    }

    public boolean containsPartner(final UserAccount userAccount) {
        return partners.contains(userAccount);
    }

    public void addPartner(final UserAccount partner) {
        Objects.requireNonNull(partner);
        if (partner.username.equals(this.username)) {
            return;
        }

        this.partners.add(partner);
        if (!partner.containsPartner(this)) {
            partner.addPartner(this);
        }
    }

    public void removePartner(final UserAccount partner) {
        Objects.requireNonNull(partner);
        partners.remove(partner);
        partner.removePartner(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;

        return isEnable == that.isEnable && Objects.equals(id, that.id) && Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) && Objects.equals(password, that.password) && userRole == that.userRole &&
                Objects.equals(rating, that.rating) && Objects.equals(accountEvents, that.accountEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, username, email, password, userRole, isEnable, rating, accountEvents
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
                    User role : %s,
                    Is enable : %s,
                    Rating : %f,
                    Creation date : %s,
                    Last updated date : %s
               }
               """,
                id,
                username.username(),
                email.email(),
                userRole,
                enables,
                rating.rating(),
                accountEvents.creationDate().toString(),
                accountEvents.lastUpdateDate().toString()
        );
    }
}