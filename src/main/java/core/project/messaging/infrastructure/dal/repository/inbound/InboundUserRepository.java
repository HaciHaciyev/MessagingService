package core.project.messaging.infrastructure.dal.repository.inbound;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.insert;

@ApplicationScoped
public class InboundUserRepository {

    private final JDBC jdbc;

    static final String INSERT_NEW_PARTNERSHIP = insert()
            .into("UserPartnership")
            .columns("user_id",
                    "partner_id",
                    "created_at")
            .values(3)
            .build();

    public InboundUserRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    public void addPartnership(UserAccount firstUser, UserAccount secondUser) {
        final boolean doNotMatch = !firstUser.getPartners().contains(secondUser) || !secondUser.getPartners().contains(firstUser);
        if (doNotMatch) {
            throw new IllegalArgumentException("Illegal function usage.");
        }

        jdbc
                .write(INSERT_NEW_PARTNERSHIP, firstUser.getId().toString(), secondUser.getId().toString(), LocalDateTime.now())
                .ifFailure(Throwable::printStackTrace);
    }
}
