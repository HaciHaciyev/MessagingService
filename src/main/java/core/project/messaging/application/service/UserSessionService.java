package core.project.messaging.application.service;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.enumerations.MessageAddressee;
import core.project.messaging.domain.services.PartnershipsService;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.SessionStorage;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static core.project.messaging.application.dto.MessageType.PARTNERSHIP_REQUEST;
import static core.project.messaging.application.util.WSUtilities.*;

@ApplicationScoped
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSessionService {

    private final SessionStorage sessionStorage;

    private final PartnershipsService partnershipsService;

    private final OutboundUserRepository outboundUserRepository;

    public void handleOnOpen(Session session, Username username) {
        CompletableFuture.runAsync(() -> {
            Result<UserAccount, Throwable> account = outboundUserRepository.findByUsername(username);
            if (!account.success()) {
                closeSession(session, Message.error("This account is do not exists."));
                return;
            }

            sessionStorage.put(session, account.orElseThrow());
            partnershipsService
                    .getAll(username.username())
                    .forEach((user, message) -> sendMessage(session, Message.info(String.format("%s: {%s}", user, message))));
        });
    }

    public void handleOnMessage(Session session, Username username, Message message) {
        final Pair<Session, UserAccount> sessionUser = Pair.of(session, sessionStorage.get(username).getSecond());

        Log.infof("Handling %s of user -> %s", message.type(), username.username());
        CompletableFuture.runAsync(() -> handleWebSocketMessage(message, sessionUser.getFirst(), sessionUser.getSecond()));
    }

    private void handleWebSocketMessage(Message message, Session session, UserAccount user) {
        if (message.type().equals(PARTNERSHIP_REQUEST)) {
            String addressee = message.partner();
            if (Objects.isNull(addressee)  || addressee.isEmpty()) {
                sendMessage(session, Message.error("Invalid partner username."));
                return;
            }

            partnershipRequest(session, user, message, new Username(addressee));
            return;
        }

        sendMessage(session, Message.error("Invalid message type."));
    }

    private void partnershipRequest(Session session, UserAccount addresser, Message message, Username addressee) {
        if (sessionStorage.contains(addressee)) {
            Pair<Session, UserAccount> addresseePair = sessionStorage.get(addressee);
            Pair<MessageAddressee, Message> messages = partnershipsService.partnershipRequest(Pair.of(session, addresser), addresseePair, message);
            send(messages, session, addresseePair.getFirst());
            return;
        }

        Pair<MessageAddressee, Message> messages = partnershipsService.partnershipRequest(Pair.of(session, addresser), addressee, message);
        send(messages, session, null);
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

    public void handleOnClose(Session session, Username username) {
        sessionStorage.remove(username);
        closeSession(session, "Session is closed.");
    }
}
