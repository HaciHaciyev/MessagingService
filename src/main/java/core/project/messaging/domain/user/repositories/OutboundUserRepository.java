package core.project.messaging.domain.user.repositories;

import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.value_objects.Email;

import java.util.List;
import java.util.UUID;

public interface OutboundUserRepository {

    boolean isEmailExists(String verifiableEmail);

    boolean isUsernameExists(String verifiableUsername);

    Result<List<String>, Throwable> listOfPartners(String username, int limit, int offSet);

    boolean havePartnership(User user, User partner);

    Result<User, Throwable> findById(UUID userId);

    Result<User, Throwable> findByUsername(String username);

    Result<User, Throwable> findByEmail(Email email);
}
