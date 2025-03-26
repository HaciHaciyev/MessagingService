package core.project.messaging.domain.user.repositories;

import core.project.messaging.domain.user.entities.UserAccount;
import core.project.messaging.domain.user.value_objects.Email;
import core.project.messaging.infrastructure.utilities.containers.Result;

import java.util.List;
import java.util.UUID;

public interface OutboundUserRepository {

    boolean isEmailExists(String verifiableEmail);

    boolean isUsernameExists(String verifiableUsername);

    Result<List<String>, Throwable> listOfPartners(String username, int limit, int offSet);

    boolean havePartnership(UserAccount user, UserAccount partner);

    Result<UserAccount, Throwable> findById(UUID userId);

    Result<UserAccount, Throwable> findByUsername(String username);

    Result<UserAccount, Throwable> findByEmail(Email email);
}
