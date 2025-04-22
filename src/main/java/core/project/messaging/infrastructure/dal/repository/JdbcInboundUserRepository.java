package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jdbclight.jdbc.JDBC;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.repositories.InboundUserRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

import static com.hadzhy.jdbclight.sql.SQLBuilder.delete;
import static com.hadzhy.jdbclight.sql.SQLBuilder.insert;

@ApplicationScoped
public class JdbcInboundUserRepository implements InboundUserRepository {

    private final JDBC jdbc;

    static final String INSERT_NEW_PARTNERSHIP = insert()
            .into("UserPartnership")
            .columns("user_id", "partner_id", "created_at")
            .values()
            .build()
            .sql();

    static final String DELETE_PARTNERSHIP = delete()
            .from("UserPartnership")
            .where("(user_id = ?")
            .and("partner_id = ?)")
            .or("(user_id = ?")
            .and("partner_id = ?)")
            .build()
            .sql();

    public JdbcInboundUserRepository() {
        this.jdbc = JDBC.instance();
    }

    @Override
    public void addPartnership(User firstUser, User secondUser) {
        final boolean doNotMatch = !firstUser.partners().contains(secondUser) || !secondUser.partners().contains(firstUser);
        if (doNotMatch) throw new IllegalArgumentException("Illegal function usage.");

        jdbc.write(INSERT_NEW_PARTNERSHIP,
                        firstUser.id().toString(),
                        secondUser.id().toString(),
                        LocalDateTime.now())
                .ifFailure(Throwable::printStackTrace);
    }

    @Override
    public void removePartnership(User firstUser, User secondUser) {
        jdbc.write(DELETE_PARTNERSHIP,
                        firstUser.id().toString(),
                        secondUser.id().toString(),
                        secondUser.id().toString(),
                        firstUser.id().toString())
                .ifFailure(Throwable::printStackTrace);
    }
}
