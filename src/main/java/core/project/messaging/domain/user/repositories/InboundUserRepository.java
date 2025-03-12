package core.project.messaging.domain.user.repositories;

import core.project.messaging.domain.user.entities.UserAccount;

public interface InboundUserRepository {

    void addPartnership(UserAccount firstUser, UserAccount secondUser);

    void removePartnership(UserAccount firstUser, UserAccount secondUser);
}
