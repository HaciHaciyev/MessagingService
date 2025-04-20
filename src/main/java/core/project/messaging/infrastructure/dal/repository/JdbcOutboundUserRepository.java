package core.project.messaging.infrastructure.dal.repository;

import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.events.AccountEvents;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.value_objects.*;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.select;
import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.selectDistinct;

@Transactional
@ApplicationScoped
public class JdbcOutboundUserRepository implements OutboundUserRepository {

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
            .count("*")
            .from("UserPartnership")
            .where("(user_id = ?")
            .and("partner_id = ?)")
            .or("(user_id = ?")
            .and("partner_id = ?)")
            .build();

    static final String GET_PARTNERS_USERNAMES = selectDistinct()
            .caseStatement()
            .when("user_account.username = ?").then("partner.username")
            .elseCase("user_account.username")
            .endAs("username")
            .fromAs("UserPartnership", "up")
            .joinAs("UserAccount", "partner", "up.partner_id = partner.id")
            .joinAs("UserAccount", "user_account", "up.user_id = user_account.id")
            .where("user_account.username = ?")
            .or("partner.username = ?")
            .limitAndOffset();

    JdbcOutboundUserRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isEmailExists(String verifiableEmail) {
        return jdbc.readObjectOf(FIND_EMAIL, Integer.class, verifiableEmail)
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    public boolean isUsernameExists(String verifiableUsername) {
        return jdbc.readObjectOf(FIND_USERNAME, Integer.class, verifiableUsername)
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking username existence.");
                    return false;
                });
    }

    public Result<List<String>, Throwable> listOfPartners(String username, int limit, int offSet) {
        return jdbc.readListOf(GET_PARTNERS_USERNAMES,
                rs -> rs.getString("username"),
                Objects.requireNonNull(username), username, username, limit, offSet
        );
    }

    public boolean havePartnership(User user, User partner) {
        return jdbc.readObjectOf(IS_PARTNERSHIP_EXISTS,
                        Integer.class,
                        user.getId().toString(),
                        partner.getId().toString(),
                        partner.getId().toString(),
                        user.getId().toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking partnership existence.");
                    return false;
                });
    }

    public Result<User, Throwable> findById(UUID userId) {
        return jdbc.read(FIND_BY_ID, this::userAccountMapper, userId.toString());
    }

    public Result<User, Throwable> findByUsername(String username) {
        return jdbc.read(FIND_BY_USERNAME, this::userAccountMapper, username);
    }

    public Result<User, Throwable> findByEmail(Email email) {
        return jdbc.read(FIND_BY_EMAIL, this::userAccountMapper, email.email());
    }

    private User userAccountMapper(final ResultSet rs) throws SQLException {
        var events = new AccountEvents(
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("last_updated_date", Timestamp.class).toLocalDateTime()
        );

        var rating = Rating.fromRepository(
                rs.getDouble("rating"),
                rs.getDouble("rating_deviation"),
                rs.getDouble("rating_volatility")
        );

        return new User(
                UUID.fromString(rs.getString("id")),
                new Firstname(rs.getString("firstname")),
                new Surname(rs.getString("surname")),
                new Username(rs.getString("username")),
                new Email(rs.getString("email")),
                new Password(rs.getString("password")),
                rs.getBoolean("is_enable"),
                rating,
                events
        );
    }
}
