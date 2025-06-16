package core.project.messaging.application.controller.ws;

import core.project.messaging.application.dto.messaging.Message;
import core.project.messaging.application.service.UserSessionService;
import core.project.messaging.application.service.WSAuthService;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.ws.MessageDecoder;
import core.project.messaging.infrastructure.ws.MessageEncoder;
import core.project.messaging.infrastructure.ws.RateLimiter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

import static core.project.messaging.application.util.WSUtilities.closeSession;
import static core.project.messaging.application.util.WSUtilities.sendMessage;

@ServerEndpoint(value = "/chessland/user-session", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class UserSessionHandler {

    private final WSAuthService authService;

    private final RateLimiter rateLimiter;

    private final UserSessionService userSessionService;

    UserSessionHandler(RateLimiter rateLimiter,
                       WSAuthService authService,
                       UserSessionService userSessionService) {
        this.rateLimiter = rateLimiter;
        this.authService = authService;
        this.userSessionService = userSessionService;
    }

    @OnOpen
    @WithSpan("MESSAGING OPEN")
    public final void onOpen(Session session) {
        Thread.startVirtualThread(() ->
                authService.validateToken(session)
                        .handle(token -> userSessionService.onOpen(session, new Username(token.getName())),
                                throwable -> closeSession(session, Message.error(throwable.getLocalizedMessage())))
        );
    }

    @OnMessage
    @WithSpan("MESSAGING MESSAGE")
    public final void onMessage(Session session, Message message) {
        Span.current().setAttribute("message.type", message.type().name());

        Thread.startVirtualThread(() -> {
            Result<JsonWebToken, IllegalStateException> parseResult = authService.validateToken(session);
            if (!parseResult.success()) {
                closeSession(session, Message.error(parseResult.throwable().getLocalizedMessage()));
                return;
            }

            Username username = new Username(parseResult.value().getName());
            Optional<User> findUser = userSessionService.extractAccount(session);
            if (findUser.isEmpty()) {
                closeSession(session, Message.error("Session do not contains user account."));
                return;
            }

            User user = findUser.get();
            final boolean isRateDoNotLimited = rateLimiter.tryAcquire(user);
            if (!isRateDoNotLimited) {
                sendMessage(session, Message.error("You expose of message limits per time unit."));
                return;
            }

            userSessionService.onMessage(session, username, message);
        });
    }

    @OnClose
    @WithSpan("MESSAGING CLOSE")
    public final void onClose(Session session) {
        authService.validateToken(session).handle(
                token -> userSessionService.onClose(session, new Username(token.getName())),
                throwable -> closeSession(session, Message.error(throwable.getLocalizedMessage()))
        );
    }
}