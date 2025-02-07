package core.project.messaging.application.service;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.enumerations.MessageAddressee;
import core.project.messaging.domain.services.PartnershipsService;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.SessionStorage;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import core.project.messaging.infrastructure.security.JwtUtility;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static core.project.messaging.application.util.WSUtilities.*;

@ApplicationScoped
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSessionService {

    private final JwtUtility jwtUtility;

    private final SessionStorage sessionStorage;

    private final PartnershipsService partnershipsService;

    private final OutboundUserRepository outboundUserRepository;

    public void onOpen(Session session, Username username) {
        CompletableFuture.runAsync(() -> {
            Result<UserAccount, Throwable> account = outboundUserRepository.findByUsername(username);
            if (!account.success()) {
                closeSession(session, "This account does`t exist.");
                return;
            }

            sessionStorage.put(session, Objects.requireNonNull(account.orElseThrow()));
            partnershipsService
                    .getAll(username.username())
                    .forEach((user, message) -> sendMessage(session, Message.info(String.format("%s: {%s}", user, message))));
        });
    }

    public void onMessage(Session session, Username username, Message message) {
        Log.infof("Handling %s of user -> %s.", message.type(), username.username());

        Optional<UserAccount> userAccount = extractAccount(session);
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
                    sessionStorage.remove(session);
                    return Optional.empty();
                });
    }

    private void handleMessage(Message message, Session session, UserAccount user) {
        switch (message.type()) {
            case PARTNERSHIP_REQUEST -> {
                String addressee = message.partner();
                if (Username.validate(addressee)) {
                    sendMessage(session, Message.error("Partner user name is required for partnership creation."));
                    return;
                }

                partnershipRequest(session, user, message, new Username(addressee));
            }
            case PARTNERSHIP_DECLINE -> {
                String addressee = message.partner();
                if (Username.validate(addressee)) {
                    sendMessage(session, Message.error("Partner user name is required for partnership declining."));
                    return;
                }

                partnershipsService.partnershipDecline(user, new Username(addressee));
            }
            default -> sendMessage(session, Message.error("Invalid message type."));
        }
    }

    private void partnershipRequest(Session session, UserAccount addresser, Message message, Username addressee) {
        if (sessionStorage.contains(addressee)) {
            Optional<Session> addresseeSession = sessionStorage.get(addressee);
            if (addresseeSession.isEmpty()) {
                closeAndRemoveSession("Unexpected error. The connected web socket connection is not in the storage.", session);
                return;
            }

            Optional<UserAccount> addresseeAccount = extractAccount(addresseeSession.orElseThrow());
            if (addresseeAccount.isEmpty()) {
                closeAndRemoveSession("Unexpected error. The connected web socket connection is not in the storage.", addresseeSession.orElseThrow());
                return;
            }

            Pair<MessageAddressee, Message> messages = partnershipsService.partnershipRequest(addresser, addresseeAccount.orElseThrow(), message);
            send(messages, session, addresseeSession.orElseThrow());
            return;
        }

        Pair<MessageAddressee, Message> messages = partnershipsService.partnershipRequest(addresser, addressee, message);
        send(messages, session, null);
    }

    public void handleOnClose(Session session) {
        sessionStorage.remove(session);
        closeSession(session, "Session is closed.");
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

    public Optional<UserAccount> extractAccount(Session session) {
        return Optional.ofNullable(session.getUserProperties().get(SessionStorage.SessionProperties.USER_ACCOUNT.key()))
                .filter(UserAccount.class::isInstance)
                .map(UserAccount.class::cast);
    }
}
