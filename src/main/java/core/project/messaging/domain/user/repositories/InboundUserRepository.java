package core.project.messaging.domain.user.repositories;

import core.project.messaging.domain.user.entities.User;

public interface InboundUserRepository {

    void addPartnership(User firstUser, User secondUser);

    void removePartnership(User firstUser, User secondUser);
}
