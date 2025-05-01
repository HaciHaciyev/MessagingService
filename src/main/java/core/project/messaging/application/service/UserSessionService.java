package core.project.messaging.application.service;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.commons.tuples.Pair;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.enumerations.MessageAddressee;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.services.PartnershipsService;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.SessionStorage;
import core.project.messaging.infrastructure.security.JWTUtility;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static core.project.messaging.application.util.WSUtilities.closeSession;
import static core.project.messaging.application.util.WSUtilities.sendMessage;

@ApplicationScoped
public class UserSessionService {

    private final JWTUtility jwtUtility;

    private final SessionStorage sessionStorage;

    private final PartnershipsService partnershipsService;

    private final OutboundUserRepository outboundUserRepository;

    UserSessionService(JWTUtility jwtUtility, SessionStorage sessionStorage,
                       PartnershipsService partnershipsService,
                       OutboundUserRepository outboundUserRepository) {
        this.jwtUtility = jwtUtility;
        this.sessionStorage = sessionStorage;
        this.partnershipsService = partnershipsService;
        this.outboundUserRepository = outboundUserRepository;
    }

    public void onOpen(Session session, Username username) {
        CompletableFuture.runAsync(() -> {
            Result<User, Throwable> account = outboundUserRepository.findByUsername(username.username());
            if (!account.success()) {
                closeSession(session, Message.error("This account does`t exist."));
                return;
            }
            if (sessionStorage.contains(username)) {
                closeSession(session, Message.error("You can`t duplicate sessions."));
                return;
            }

            sessionStorage.put(session, Objects.requireNonNull(account.orElseThrow()));
            partnershipsService
                    .getAll(username.username())
                    .forEach((user, message) ->
                            sendMessage(session, Message.partnershipRequest(String.format("%s: {%s}", user, message), user)));
        });
    }

    public void onMessage(Session session, Username username, Message message) {
        Log.infof("Handling %s of user -> %s.", message.type(), username.username());

        Optional<User> userAccount = extractAccount(session);
        if (userAccount.isEmpty()) {
            closeAndRemoveSession("Unexpected error. Session does not have a user account.", session);
            return;
        }

        CompletableFuture.runAsync(() -> handleMessage(message, session, userAccount.orElseThrow()));
    }

    public Optional<JsonWebToken> validateToken(Session session) {
        return jwtUtility
                .extractJWT(session)
                .or(() -> {
                    closeSession(session, Message.error("You are not authorized. Token is required."));
                    return Optional.empty();
                });
    }

    private void handleMessage(Message message, Session session, User user) {
        switch (message.type()) {
            case PARTNERSHIP_REQUEST -> {
                String addressee = message.partner();
                if (!Username.validate(addressee)) {
                    sendMessage(session, Message.error("Partner user name is required for partnership creation."));
                    return;
                }

                partnershipRequest(session, user, message, new Username(addressee));
            }
            case PARTNERSHIP_DECLINE -> {
                String addressee = message.partner();
                if (!Username.validate(addressee)) {
                    sendMessage(session, Message.error("Partner user name is required for partnership declining."));
                    return;
                }

                partnershipsService.partnershipDecline(user, new Username(addressee));
            }
            default -> sendMessage(session, Message.error("Invalid message type."));
        }
    }

    private void partnershipRequest(Session session, User addresser, Message message, Username addressee) {
        if (sessionStorage.contains(addressee)) {
            Optional<Session> addresseeSession = sessionStorage.get(addressee);
            if (addresseeSession.isEmpty()) {
                closeAndRemoveSession("Unexpected error. The connected web socket connection is not in the storage.", session);
                return;
            }

            Optional<User> addresseeAccount = extractAccount(addresseeSession.orElseThrow());
            if (addresseeAccount.isEmpty()) {
                closeAndRemoveSession("Unexpected error. The connected web socket connection is not in the storage.",
                        addresseeSession.orElseThrow());
                return;
            }

            Pair<MessageAddressee, Message> messages = partnershipsService
                    .partnershipRequest(addresser, addresseeAccount.orElseThrow(), message);
            send(messages, session, addresseeSession.orElseThrow());
            return;
        }

        Pair<MessageAddressee, Message> messages = partnershipsService.partnershipRequest(addresser, addressee, message);
        send(messages, session, null);
    }

    public void handleOnClose(Session session) {
        sessionStorage.remove(session);
    }

    private static void send(Pair<MessageAddressee, Message> messages, Session addresser, @Nullable Session addressee) {
        switch (messages.getFirst()) {
            case FOR_ALL -> {
                sendMessage(addresser, messages.getSecond());
                sendMessage(addressee, messages.getSecond());
            }
            case ONLY_ADDRESSER -> sendMessage(addresser, messages.getSecond());
            case ONLY_ADDRESSEE -> sendMessage(addressee, messages.getSecond());
        }
    }

    private void closeAndRemoveSession(String message, Session session) {
        Log.error(message);
        closeSession(session, Message.error(message));
        sessionStorage.remove(session);
    }

    public Optional<User> extractAccount(Session session) {
        return Optional.ofNullable(session.getUserProperties().get(SessionStorage.SessionProperties.USER_ACCOUNT.key()))
                .filter(User.class::isInstance)
                .map(User.class::cast);
    }
}
