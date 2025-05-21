package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.repositories.InboundUserRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

import static com.hadzhy.jetquerious.sql.QueryForge.delete;
import static com.hadzhy.jetquerious.sql.QueryForge.insert;

@ApplicationScoped
public class JdbcInboundUserRepository implements InboundUserRepository {

    private final JetQuerious jet;

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

    JdbcInboundUserRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public void addPartnership(User firstUser, User secondUser) {
        final boolean doNotMatch = !firstUser.partners().contains(secondUser) || !secondUser.partners().contains(firstUser);
        if (doNotMatch) throw new IllegalArgumentException("Illegal function usage.");

        jet.write(INSERT_NEW_PARTNERSHIP,
                        firstUser.id().toString(),
                        secondUser.id().toString(),
                        LocalDateTime.now())
                .ifFailure(Throwable::printStackTrace);
    }

    @Override
    public void removePartnership(User firstUser, User secondUser) {
        jet.write(DELETE_PARTNERSHIP,
                        firstUser.id().toString(),
                        secondUser.id().toString(),
                        secondUser.id().toString(),
                        firstUser.id().toString())
                .ifFailure(Throwable::printStackTrace);
    }
}
