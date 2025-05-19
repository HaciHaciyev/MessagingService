package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.events.AccountEvents;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.value_objects.*;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static com.hadzhy.jetquerious.sql.QueryForge.selectDistinct;

@Transactional
@ApplicationScoped
public class JdbcOutboundUserRepository implements OutboundUserRepository {

    private final JetQuerious jet;

    static final String FIND_BY_USERNAME = select()
            .all()
            .from("UserAccount")
            .where("username = ?")
            .build()
            .sql();

    static final String IS_PARTNERSHIP_EXISTS = select()
            .count("*")
            .from("UserPartnership")
            .where("(user_id = ?")
            .and("partner_id = ?)")
            .or("(user_id = ?")
            .and("partner_id = ?)")
            .build()
            .sql();

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
            .limitAndOffset()
            .sql();

    JdbcOutboundUserRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<List<String>, Throwable> listOfPartners(Username username, int limit, int offSet) {
        var result = jet.readListOf(GET_PARTNERS_USERNAMES,
                rs -> rs.getString("username"),
                Objects.requireNonNull(username.username()), username.username(), username.username(), limit, offSet
        );
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public boolean havePartnership(User user, User partner) {
        return jet.readObjectOf(IS_PARTNERSHIP_EXISTS,
                        Integer.class,
                        user.id().toString(),
                        partner.id().toString(),
                        partner.id().toString(),
                        user.id().toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking partnership existence.");
                    return false;
                });
    }

    @Override
    public Result<User, Throwable> findByUsername(Username username) {
        var result = jet.read(FIND_BY_USERNAME, this::userAccountMapper, username.username());
        return new Result<>(result.value(), result.throwable(), result.success());
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
