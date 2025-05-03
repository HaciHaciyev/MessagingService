package core.project.messaging.domain.user.repositories;

import core.project.messaging.domain.commons.containers.StatusPair;
import core.project.messaging.domain.user.value_objects.Username;

import java.util.Map;

public interface PartnershipRequestsRepository {

    void put(Username addressee, Username addresser, String message);

    StatusPair<String> get(Username addressee, Username addresser);

    Map<String, String> getAll(Username addressee);

    void delete(Username addressee, Username addresser);
}
