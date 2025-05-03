package core.project.messaging.domain.user.repositories;

import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.value_objects.Username;

import java.util.List;

public interface OutboundUserRepository {

    Result<List<String>, Throwable> listOfPartners(Username username, int limit, int offSet);

    boolean havePartnership(User user, User partner);

    Result<User, Throwable> findByUsername(Username username);
}
