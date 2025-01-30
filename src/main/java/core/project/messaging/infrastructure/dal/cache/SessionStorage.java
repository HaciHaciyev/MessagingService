package core.project.messaging.infrastructure.dal.cache;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionStorage {

    private static final ConcurrentHashMap<Username, Session> sessions = new ConcurrentHashMap<>();

    public void put(final Session session, final UserAccount userAccount) {
        session.getUserProperties().put(SessionProperties.USER_ACCOUNT.key(), userAccount);
        sessions.put(userAccount.getUsername(), session);
    }

    public Optional<Session> get(final Username username) {
        return Optional.ofNullable(sessions.get(username));
    }

    public boolean contains(final Username username) {
        return sessions.containsKey(username);
    }

    public void remove(final Session session) {
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
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
