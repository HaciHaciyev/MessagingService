package core.project.messaging.infrastructure.dal.repository.outbound;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.events.AccountEvents;
import core.project.messaging.domain.value_objects.*;
import core.project.messaging.infrastructure.dal.JDBC;
import core.project.messaging.infrastructure.exceptions.DataNotFoundException;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class OutboundUserRepository {

    private final JDBC jdbc;

    private static final String FIND_EMAIL = "SELECT COUNT(email) FROM UserAccount WHERE email = ?";
    private static final String FIND_USERNAME = "SELECT COUNT(username) FROM UserAccount WHERE username = ?";
    private static final String FIND_BY_ID = "SELECT * FROM UserAccount WHERE id = ?";
    private static final String FIND_BY_USERNAME = "SELECT * FROM UserAccount WHERE username = ?";
    private static final String FIND_BY_EMAIL = "SELECT * FROM UserAccount WHERE email = ?";
    private static final String IS_PARTNERSHIP_EXISTS = """
            SELECT * FROM UserPartnership
            WHERE (user_id = ? AND partner_id = ?) OR (user_id = ? AND partner_id = ?);
            """;

    OutboundUserRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isEmailExists(Email verifiableEmail) {
        Result<Integer, Throwable> result = jdbc.readObjectOf(
                FIND_EMAIL,
                Integer.class,
                verifiableEmail.email()
        );

        if (!result.success()) {

            if (result.throwable() instanceof DataNotFoundException) {
                return false;
            } else {
                Log.info(result.throwable());
            }

        }

        return result.value() != null && result.value() > 0;
    }

    public boolean isUsernameExists(Username verifiableUsername) {
        Result<Integer, Throwable> result = jdbc.readObjectOf(
                FIND_USERNAME,
                Integer.class,
                verifiableUsername.username()
        );

        if (!result.success()) {

            if (result.throwable() instanceof DataNotFoundException) {
                return false;
            } else {
                Log.info(result.throwable());
            }

        }

        return result.value() != null && result.value() > 0;
    }

    public boolean havePartnership(UserAccount user, UserAccount partner) {
        Result<Boolean, Throwable> result = jdbc.readObjectOf(
                IS_PARTNERSHIP_EXISTS, Boolean.class, user.getId(), partner.getId(), partner.getId(), user.getId()
        );

        if (!result.success()) {
            if (result.throwable() instanceof DataNotFoundException) {
                return false;
            } else {
                Log.info(result.throwable());
            }
        }

        return result.value();
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
