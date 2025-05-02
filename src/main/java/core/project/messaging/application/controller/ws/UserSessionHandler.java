package core.project.messaging.application.controller.ws;

import core.project.messaging.application.dto.Message;
import core.project.messaging.application.service.UserSessionService;
import core.project.messaging.application.service.WSAuthService;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.ws.MessageDecoder;
import core.project.messaging.infrastructure.ws.MessageEncoder;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import static core.project.messaging.application.util.WSUtilities.closeSession;

@ServerEndpoint(value = "/chessland/user-session", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class UserSessionHandler {

    private final WSAuthService authService;

    private final UserSessionService userSessionService;

    UserSessionHandler(WSAuthService authService, UserSessionService userSessionService) {
        this.authService = authService;
        this.userSessionService = userSessionService;
    }

    @OnOpen
    public final void onOpen(Session session) {
        authService.validateToken(session)
                .handle(token -> userSessionService.onOpen(session, new Username(token.getName())),
                        throwable -> closeSession(session, Message.error(throwable.getLocalizedMessage()))
                );
    }

    @OnMessage
    public final void onMessage(Session session, Message message) {
        authService.validateToken(session)
                .handle(token -> userSessionService.onMessage(session, new Username(token.getName()), message),
                        throwable -> closeSession(session, Message.error(throwable.getLocalizedMessage()))
                );
    }

    @OnClose
    public final void onClose(Session session) {
        authService.validateToken(session)
                .handle(token -> userSessionService.onClose(session, new Username(token.getName())),
                        throwable -> closeSession(session, Message.error(throwable.getLocalizedMessage()))
                );
    }
}