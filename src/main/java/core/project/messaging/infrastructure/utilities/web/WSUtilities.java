package core.project.messaging.infrastructure.utilities.web;

import io.quarkus.logging.Log;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

public class WSUtilities {

    private WSUtilities() {}

    /**
     * Closes session with specified message as reason (can be null)
     */
    public static void closeSession(final Session currentSession, final String message) {
        try {
            currentSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, message));
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    /**
     * Sends message to specified session
     */
    public static void sendMessage(final Session session, final String message) {
        try {
            session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            Log.info(e.getMessage());
        }
    }
}
