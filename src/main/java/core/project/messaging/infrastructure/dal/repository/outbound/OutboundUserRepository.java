package core.project.messaging.infrastructure.dal.repository.outbound;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.enumerations.UserRole;
import core.project.messaging.domain.events.AccountEvents;
import core.project.messaging.domain.value_objects.*;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.select;

@Transactional
@ApplicationScoped
public class OutboundUserRepository {

    private final JDBC jdbc;

    static final String FIND_EMAIL = select()
            .count("*")
            .from("UserAccount")
            .where("email = ?")
            .build();

    static final String FIND_USERNAME = select()
            .count("*")
            .from("UserAccount")
            .where("username = ?")
            .build();

    static final String FIND_BY_ID = select()
            .all()
            .from("UserAccount")
            .where("id = ?")
            .build();

    static final String FIND_BY_USERNAME = select()
            .all()
            .from("UserAccount")
            .where("username = ?")
            .build();

    static final String FIND_BY_EMAIL = select()
            .all()
            .from("UserAccount")
            .where("email = ?")
            .build();

    static final String IS_PARTNERSHIP_EXISTS = select()
            .all()
            .from("UserPartnership")
            .where("(user_id = ?")
            .and("partner_id = ?)")
            .or("(user_id = ?")
            .and("partner_id = ?)")
            .build();

    OutboundUserRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isEmailExists(Email verifiableEmail) {
        return jdbc.readObjectOf(FIND_EMAIL, Integer.class, verifiableEmail.email())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    public boolean isUsernameExists(Username verifiableUsername) {
        return jdbc.readObjectOf(FIND_USERNAME, Integer.class, verifiableUsername.username())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking username existence.");
                    return false;
                });
    }

    public boolean havePartnership(UserAccount user, UserAccount partner) {
        return jdbc.readObjectOf(IS_PARTNERSHIP_EXISTS,
                Boolean.class,
                user.getId(),
                partner.getId(),
                partner.getId(),
                user.getId())
                .value();
    }

    public Result<UserAccount, Throwable> findById(UUID userId) {
        return jdbc.read(FIND_BY_ID, this::userAccountMapper, userId.toString());
    }

    public Result<UserAccount, Throwable> findByUsername(Username username) {
        return jdbc.read(FIND_BY_USERNAME, this::userAccountMapper, username.username());
    }

    public Result<UserAccount, Throwable> findByEmail(Email email) {
        return jdbc.read(FIND_BY_EMAIL, this::userAccountMapper, email.email());
    }

    private UserAccount userAccountMapper(final ResultSet rs) throws SQLException {
        Log.infof("The user account %s was taken from the database", rs.getString("username"));

        var events = new AccountEvents(
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("last_updated_date", Timestamp.class).toLocalDateTime()
        );

        var rating = Rating.fromRepository(
                rs.getDouble("rating"),
                rs.getDouble("rating_deviation"),
                rs.getDouble("rating_volatility")
        );

        return UserAccount.fromRepository(
                UUID.fromString(rs.getString("id")),
                new Username(rs.getString("username")),
                new Email(rs.getString("email")),
                new Password(rs.getString("password")),
                UserRole.valueOf(rs.getString("user_role")),
                rs.getBoolean("is_enable"),
                rating,
                events
        );
    }
}
