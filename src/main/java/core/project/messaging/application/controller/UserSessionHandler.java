package core.project.messaging.application.controller;

import core.project.messaging.application.dto.Message;
import core.project.messaging.application.service.UserSessionService;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.config.application.MessageDecoder;
import core.project.messaging.infrastructure.config.application.MessageEncoder;
import core.project.messaging.infrastructure.config.security.JwtUtility;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

import static core.project.messaging.infrastructure.utilities.web.WSUtilities.closeSession;
import static core.project.messaging.infrastructure.utilities.web.WSUtilities.sendMessage;

@ServerEndpoint(value = "/user-session", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSessionHandler {

    private final JwtUtility jwtUtility;
    private final UserSessionService userSessionService;

    @OnOpen
    public final void onOpen(Session session) {
        validateToken(session)
                .ifPresent(token -> userSessionService.handleOnOpen(session, new Username(token.getName())));
    }

    @OnMessage
    public final void onMessage(Session session, Message message) {
        validateToken(session)
                .ifPresent(token -> userSessionService.handleOnMessage(session, new Username(token.getName()), message));
    }

    @OnClose
    public final void onClose(Session session) {
        validateToken(session)
                .ifPresent(token -> userSessionService.handleOnClose(session, new Username(token.getName())));
    }

    private Optional<JsonWebToken> validateToken(Session session) {
        final Optional<JsonWebToken> jwt = jwtUtility.extractJWT(session);

        if (jwt.isEmpty()) {
            sendMessage(session, Message.error("Token is required."));
            closeSession(session, Message.error("You are not authorized."));
            return Optional.empty();
        }

        return jwt;
    }
}