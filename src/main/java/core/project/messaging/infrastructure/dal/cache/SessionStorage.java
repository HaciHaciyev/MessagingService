package core.project.messaging.infrastructure.dal.cache;

import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionStorage {

    private static final ConcurrentHashMap<Username, Session> sessions = new ConcurrentHashMap<>();

    public void put(final Session session, final User user) {
        session.getUserProperties().put(SessionProperties.USER_ACCOUNT.key(), user);
        sessions.put(user.username(), session);
    }

    public Optional<Session> get(final Username username) {
        return Optional.ofNullable(sessions.get(username));
    }

    public boolean contains(final Username username) {
        return sessions.containsKey(username);
    }

    public void remove(final Username username) {
        sessions.remove(username);
    }

    public enum SessionProperties {
        USER_ACCOUNT("account");

        private final String key;

        SessionProperties(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
