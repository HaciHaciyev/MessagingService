package core.project.messaging.infrastructure.dal.cache;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionStorage {

    private static final ConcurrentHashMap<Username, Pair<Session, UserAccount>> sessions = new ConcurrentHashMap<>();

    public void put(final Session session, final UserAccount userAccount) {
        sessions.put(userAccount.getUsername(), Pair.of(session, userAccount));
    }

    public Pair<Session, UserAccount> get(final Username username) {
        return sessions.get(username);
    }

    public boolean contains(final Username username) {
        return sessions.containsKey(username);
    }

    public Pair<Session, UserAccount> remove(final Username username) {
        return sessions.remove(username);
    }
}
