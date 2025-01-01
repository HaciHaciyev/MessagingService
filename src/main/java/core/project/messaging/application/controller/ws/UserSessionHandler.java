package core.project.messaging.application.controller.ws;

import core.project.messaging.application.dto.Message;
import core.project.messaging.application.service.UserSessionService;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.security.JwtUtility;
import core.project.messaging.infrastructure.ws.MessageDecoder;
import core.project.messaging.infrastructure.ws.MessageEncoder;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

import static core.project.messaging.application.util.WSUtilities.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ServerEndpoint(value = "/user-session", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class UserSessionHandler {

    private final JwtUtility jwtUtility;

    private final UserSessionService userSessionService;

    @OnOpen
    public final void onOpen(Session session) {
        validateToken(session).ifPresent(token -> userSessionService.onOpen(session, new Username(token.getName())));
    }

    @OnMessage
    public final void onMessage(Session session, Message message) {
        validateToken(session).ifPresent(token -> userSessionService.onMessage(session, new Username(token.getName()), message));
    }

    @OnClose
    public final void onClose(Session session) {
        validateToken(session).ifPresent(token -> userSessionService.handleOnClose(session, new Username(token.getName())));
    }

    private Optional<JsonWebToken> validateToken(Session session) {
        return jwtUtility
                .extractJWT(session)
                .or(() -> {
                    closeSession(session, Message.error("You are not authorized. Token is required."));
                    return Optional.empty();
                });
    }
}