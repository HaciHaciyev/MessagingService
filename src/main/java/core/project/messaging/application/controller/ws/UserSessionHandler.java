package core.project.messaging.application.controller.ws;

import core.project.messaging.application.dto.Message;
import core.project.messaging.application.service.UserSessionService;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.ws.MessageDecoder;
import core.project.messaging.infrastructure.ws.MessageEncoder;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ServerEndpoint(value = "/chessland/user-session", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class UserSessionHandler {

    private final UserSessionService userSessionService;

    @OnOpen
    public final void onOpen(Session session) {
        userSessionService.validateToken(session).ifPresent(token -> userSessionService.onOpen(session, new Username(token.getName())));
    }

    @OnMessage
    public final void onMessage(Session session, Message message) {
        userSessionService.validateToken(session).ifPresent(token -> userSessionService.onMessage(session, new Username(token.getName()), message));
    }

    @OnClose
    public final void onClose(Session session) {
        userSessionService.handleOnClose(session);
    }
}