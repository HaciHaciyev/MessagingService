package core.project.messaging.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import core.project.messaging.application.dto.Message;
import core.project.messaging.application.dto.MessageType;
import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.cache.MessagesService;
import core.project.messaging.infrastructure.cache.PartnershipRequestsService;
import core.project.messaging.infrastructure.repository.inbound.InboundUserRepository;
import core.project.messaging.infrastructure.repository.outbound.OutboundUserRepository;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import core.project.messaging.infrastructure.utilities.containers.StatusPair;
import core.project.messaging.infrastructure.utilities.json.JsonUtilities;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static core.project.messaging.application.dto.MessageType.PARTNERSHIP_REQUEST;
import static core.project.messaging.infrastructure.utilities.web.WSUtilities.closeSession;
import static core.project.messaging.infrastructure.utilities.web.WSUtilities.sendMessage;

@ApplicationScoped
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSessionService {

    private final MessagesService messagesService;

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    private final PartnershipRequestsService partnershipRequestsService;

    private static final ConcurrentHashMap<Username, Pair<Session, UserAccount>> sessions = new ConcurrentHashMap<>();

    public void handleOnOpen(Session session, Username username) {
        CompletableFuture.runAsync(() -> {
            Result<UserAccount, Throwable> result = outboundUserRepository.findByUsername(username);
            if (!result.success()) {
                sendMessage(session, "This account is do not founded.");
                return;
            }

            sessions.put(username, Pair.of(session, result.value()));
            CompletableFuture.runAsync(() -> messages(session, username));
        });
    }

    private void messages(Session session, Username username) {
        messagesService.pollAll(username.username()).forEach((user, message) -> sendMessage(session, String.format("%s: {%s}", user, message)));
        partnershipRequestsService.getAll(username.username()).forEach((user, message) -> sendMessage(session, String.format("%s: {%s}", user, message)));
    }

    public void handleOnMessage(Session session, Username username, String message) {
        final Pair<Session, UserAccount> sessionUser = sessions.get(username);

        final Result<MessageType, Throwable> messageType = JsonUtilities.messageType(message);
        if (!messageType.success()) {
            sendMessage(session, "Invalid message type.");
            return;
        }

        final Result<JsonNode, Throwable> messageNode = JsonUtilities.jsonTree(message);
        if (!messageNode.success()) {
            sendMessage(session, "Invalid message.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            Log.debugf("Handling %s for user {%s}", messageType, username.username());
            handleWebSocketMessage(messageNode.value(), messageType.value(), sessionUser.getFirst(), sessionUser.getSecond());
        });
    }

    private void handleWebSocketMessage(JsonNode messageNode, MessageType type, Session session, UserAccount user) {
        final Result<String, Throwable> message = JsonUtilities.message(messageNode);
        if (!message.success()) {
            sendMessage(session, "Message can`t be null.");
            return;
        }

        if (type.equals(PARTNERSHIP_REQUEST)) {
            final Result<Username, Throwable> addressee = JsonUtilities.usernameOfPartner(messageNode);
            if (!addressee.success()) {
                sendMessage(session, "Invalid partner username.");
                return;
            }

            partnershipRequest(session, user, message.value(), addressee.value());
            return;
        }

        sendMessage(session, "Invalid message type.");
    }

    private void partnershipRequest(Session session, UserAccount addresser, String message, Username addressee) {
        final Message messageWrap = Result.ofThrowable(() -> new Message(message)).orElse(null);
        if (Objects.isNull(messageWrap)) {
            sendMessage(session, "Invalid message.");
            return;
        }

        if (sessions.containsKey(addressee)) {
            processPartnershipRequest(Pair.of(session, addresser), sessions.get(addressee), messageWrap);
            return;
        }

        processPartnershipRequest(Pair.of(session, addresser), addressee, messageWrap);
    }

    private void processPartnershipRequest(final Pair<Session, UserAccount> addresserPair,
                                           final Pair<Session, UserAccount> addresseePair,
                                           final Message message) {

        final UserAccount addresserAccount = addresserPair.getSecond();
        final String addresser = addresserAccount.getUsername().username();

        final UserAccount addresseeAccount = addresseePair.getSecond();
        final String addressee = addresseeAccount.getUsername().username();

        partnershipRequestsService.put(addressee, addresser, message.message());

        final StatusPair<String> isPartnershipCreated = isPartnershipCreated(addresser, addressee);
        if (isPartnershipCreated.status()) {
            addresserAccount.addPartner(addresseeAccount);
            addresseeAccount.addPartner(addresserAccount);
            inboundUserRepository.addPartnership(addresseeAccount, addresserAccount);

            final String messageOfResult = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            sendMessage(addresserPair.getFirst(), messageOfResult);
            sendMessage(addresseePair.getFirst(), messageOfResult);

            partnershipRequestsService.delete(addressee, addresser);
            partnershipRequestsService.delete(addresser, addressee);
            return;
        }

        sendMessage(addresseePair.getFirst(), invitationMessage(message.message(), addresserAccount));
        sendMessage(addresserPair.getFirst(), String.format("Wait for user {%s} answer.", addressee));
    }

    private void processPartnershipRequest(final Pair<Session, UserAccount> addresserPair,
                                           final Username addressee,
                                           final Message message) {

        final Result<UserAccount, Throwable> result = outboundUserRepository.findByUsername(addressee);
        if (!result.success()) {
            sendMessage(addresserPair.getFirst(), "This account is not exists.");
            return;
        }

        final UserAccount addresserAccount = addresserPair.getSecond();
        final String addresser = addresserAccount.getUsername().username();

        final UserAccount addresseeAccount = result.value();

        partnershipRequestsService.put(addressee.username(), addresser, message.message());

        final StatusPair<String> isPartnershipCreated = isPartnershipCreated(addresser, addressee.username());
        if (isPartnershipCreated.status()) {
            addresserAccount.addPartner(addresseeAccount);
            addresseeAccount.addPartner(addresserAccount);
            inboundUserRepository.addPartnership(addresseeAccount, addresserAccount);

            final String messageOfResult = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            sendMessage(addresserPair.getFirst(), messageOfResult);
            messagesService.put(addressee.username(), addresser, message.message());

            partnershipRequestsService.delete(addressee.username(), addresser);
            partnershipRequestsService.delete(addresser, addressee.username());
            return;
        }

        sendMessage(addresserPair.getFirst(), String.format("Wait for user {%s} answer.", addressee));
    }

    private StatusPair<String> isPartnershipCreated(String addresser, String addressee) {
        final Map<String, String> requests = partnershipRequestsService.getAll(addresser);
        if (requests.containsKey(addressee)) {
            return StatusPair.ofTrue(requests.get(addressee));
        }

        return StatusPair.ofFalse();
    }

    private static String invitationMessage(String message, UserAccount addresser) {
        return "User {%s} invite you for partnership {%s}.".formatted(addresser.getUsername().username(), message);
    }

    private static String successfullyAddedPartnershipMessage(UserAccount firstUser, UserAccount secondUser) {
        return "Partnership {%s - %s} successfully added.".formatted(firstUser.getUsername().username(), secondUser.getUsername().username());
    }

    public void handleOnClose(Session session, Username username) {
        sessions.remove(username);
        closeSession(session, "Session is closed.");
    }
}
