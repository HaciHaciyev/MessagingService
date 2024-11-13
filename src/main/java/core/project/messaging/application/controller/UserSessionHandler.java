package core.project.messaging.application.controller;

import core.project.messaging.application.service.UserSessionService;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.config.security.JwtUtility;
import core.project.messaging.infrastructure.utilities.containers.Result;
import core.project.messaging.infrastructure.utilities.web.WSUtilities;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Objects;

@ServerEndpoint("/user-session")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSessionHandler {

    private final JwtUtility jwtUtility;
    private final UserSessionService userSessionService;

    @OnOpen
    public final void onOpen(Session session) {
        final Result<JsonWebToken, Throwable> jwt = jwtUtility.extractJWT(session);
        if (validateToken(session, jwt)) {
            return;
        }

        final Username username = new Username(jwt.value().getName());
        userSessionService.handleOnOpen(session, username);
    }

    @OnMessage
    public final void onMessage(Session session, String message) {
        if (Objects.isNull(message) || message.isBlank()) {
            WSUtilities.sendMessage(session, "Message is required.");
            return;
        }

        if (message.length() > 255) {
            WSUtilities.sendMessage(session, "This message is to long.");
            return;
        }

        final Result<JsonWebToken, Throwable> jwt = jwtUtility.extractJWT(session);
        if (validateToken(session, jwt)) {
            return;
        }

        final Username username = new Username(jwt.value().getName());
        userSessionService.handleOnMessage(session, username, message);
    }

    @OnClose
    public final void onClose(Session session) {
        final Result<JsonWebToken, Throwable> jwt = jwtUtility.extractJWT(session);
        if (validateToken(session, jwt)) {
            return;
        }

        final Username username = new Username(jwt.value().getName());
        userSessionService.handleOnClose(session, username);
    }

    private static boolean validateToken(Session session, Result<JsonWebToken, Throwable> jwt) {
        if (!jwt.success()) {
            WSUtilities.sendMessage(session, "Token is required.");
            WSUtilities.closeSession(session, "You are don`t authorized.");
            return true;
        }
        return false;
    }
}