package core.project.messaging.infrastructure.dal.repository.inbound;

import core.project.messaging.domain.user.entities.UserAccount;
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
            .columns("user_id",
                    "partner_id",
                    "created_at")
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

    public void addPartnership(UserAccount firstUser, UserAccount secondUser) {
        final boolean doNotMatch = !firstUser.getPartners().contains(secondUser) || !secondUser.getPartners().contains(firstUser);
        if (doNotMatch) {
            throw new IllegalArgumentException("Illegal function usage.");
        }

        jdbc.write(INSERT_NEW_PARTNERSHIP,
                        firstUser.getId().toString(),
                        secondUser.getId().toString(),
                        LocalDateTime.now())
                .ifFailure(Throwable::printStackTrace);
    }

    public void removePartnership(UserAccount firstUser, UserAccount secondUser) {
        jdbc.write(DELETE_PARTNERSHIP,
                        firstUser.getId().toString(),
                        secondUser.getId().toString(),
                        secondUser.getId().toString(),
                        firstUser.getId().toString())
                .ifFailure(Throwable::printStackTrace);
    }
}
