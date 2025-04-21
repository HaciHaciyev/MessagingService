package core.project.messaging.infrastructure.dal.repository;

import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.repositories.InboundUserRepository;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.delete;
import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.insert;

@ApplicationScoped
public class JdbcInboundUserRepository implements InboundUserRepository {

    private final JDBC jdbc;

    static final String INSERT_NEW_PARTNERSHIP = insert()
            .into("UserPartnership")
            .columns("user_id", "partner_id", "created_at")
            .values(3)
            .build();

    static final String DELETE_PARTNERSHIP = delete()
            .from("UserPartnership")
            .where("(user_id = ?")
            .and("partner_id = ?)")
            .or("(user_id = ?")
            .and("partner_id = ?)")
            .build();

    public JdbcInboundUserRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    public void addPartnership(User firstUser, User secondUser) {
        final boolean doNotMatch = !firstUser.partners().contains(secondUser) || !secondUser.partners().contains(firstUser);
        if (doNotMatch) throw new IllegalArgumentException("Illegal function usage.");

        jdbc.write(INSERT_NEW_PARTNERSHIP,
                        firstUser.id().toString(),
                        secondUser.id().toString(),
                        LocalDateTime.now())
                .ifFailure(Throwable::printStackTrace);
    }

    public void removePartnership(User firstUser, User secondUser) {
        jdbc.write(DELETE_PARTNERSHIP,
                        firstUser.id().toString(),
                        secondUser.id().toString(),
                        secondUser.id().toString(),
                        firstUser.id().toString())
                .ifFailure(Throwable::printStackTrace);
    }
}
