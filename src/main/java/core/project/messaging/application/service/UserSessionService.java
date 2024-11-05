package core.project.messaging.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import core.project.messaging.application.dto.Message;
import core.project.messaging.application.dto.MessageType;
import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.repository.inbound.InboundUserRepository;
import core.project.messaging.infrastructure.repository.outbound.OutboundUserRepository;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import core.project.messaging.infrastructure.utilities.json.JsonUtilities;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static core.project.messaging.application.dto.MessageType.PARTNERSHIP_REQUEST;
import static core.project.messaging.infrastructure.utilities.web.WSUtilities.closeSession;
import static core.project.messaging.infrastructure.utilities.web.WSUtilities.sendMessage;

@ApplicationScoped
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSessionService {

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    private static final ConcurrentHashMap<Username, Pair<Session, UserAccount>> sessions = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Username, Pair<UserAccount, Queue<Message>>> partnershipRequests = new ConcurrentHashMap<>();

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

    public void handleOnClose(Session session, Username username) {
        sessions.remove(username);
        closeSession(session, "Session is closed.");
    }

    private void messages(Session session, Username username) {
        partnershipRequests.get(username).getSecond().forEach(message -> sendMessage(session, message.message()));
    }

    private void handleWebSocketMessage(JsonNode messageNode, MessageType type, Session session, UserAccount user) {
        final Result<String, Throwable> message = JsonUtilities.message(messageNode);
        if (!message.success()) {
            sendMessage(session, "Message can`t be null.");
            return;
        }

        if (type.equals(PARTNERSHIP_REQUEST)) {
            final Result<Username, Throwable> username = JsonUtilities.usernameOfPartner(messageNode);
            if (!username.success()) {
                sendMessage(session, "Invalid partner username.");
                return;
            }

            partnershipRequest(session, user, message.value(), username.value());
            return;
        }

        sendMessage(session, "Invalid message type.");
    }

    private void partnershipRequest(Session session, UserAccount user, String message, Username usernameOfPartner) {
        final boolean isPartnerHaveActiveSession = sessions.containsKey(usernameOfPartner);
        if (isPartnerHaveActiveSession) {
            processPartnershipRequest(Pair.of(session, user), sessions.get(usernameOfPartner), new Message(message));
            return;
        }

        processPartnershipRequest(usernameOfPartner, new Message(message), Pair.of(session, user));
    }

    private void processPartnershipRequest(final Username partner, final Message message, final Pair<Session, UserAccount> firstUserPair) {
        final Result<UserAccount, Throwable> result = outboundUserRepository.findByUsername(partner);
        if (!result.success()) {
            sendMessage(firstUserPair.getFirst(), "This account is not exists.");
            return;
        }

        final UserAccount firstUser = firstUserPair.getSecond();
        final UserAccount secondUser = result.value();

        firstUser.addPartner(secondUser);
        final boolean isPartnershipCreated = firstUser.getPartners().contains(secondUser) && secondUser.getPartners().contains(firstUser);
        if (isPartnershipCreated) {
            sendMessage(firstUserPair.getFirst(), successfullyAddedPartnershipMessage(firstUser, secondUser));
            partnershipRequests.computeIfAbsent(partner, k -> Pair.of(secondUser, new LinkedList<>())).getSecond().add(message);
            inboundUserRepository.addPartnership(firstUser, secondUser);
            return;
        }

        sendMessage(firstUserPair.getFirst(), "Wait for user %s answer.".formatted(partner.username()));
        partnershipRequests.computeIfAbsent(partner, k -> Pair.of(secondUser, new LinkedList<>())).getSecond().add(message);
    }

    private void processPartnershipRequest(final Pair<Session, UserAccount> firstUserPair,
                                           final Pair<Session, UserAccount> secondUserPair, final Message message) {

        final UserAccount firstUser = firstUserPair.getSecond();
        final UserAccount secondUser = secondUserPair.getSecond();

        firstUser.addPartner(secondUser);
        Log.infof("""
                First user partners: %s.
                Second user partners: %s.
                """, firstUser.getPartners(), secondUser.getPartners()
        );

        final boolean isPartnershipCreated = firstUser.getPartners().contains(secondUser) && secondUser.getPartners().contains(firstUser);
        if (isPartnershipCreated) {
            sendMessage(firstUserPair.getFirst(), successfullyAddedPartnershipMessage(firstUser, secondUser));
            sendMessage(secondUserPair.getFirst(), successfullyAddedPartnershipMessage(secondUser, firstUser));
            inboundUserRepository.addPartnership(firstUser, secondUser);
            return;
        }

        sendMessage(firstUserPair.getFirst(), "Wait for user answer.");
        sendMessage(secondUserPair.getFirst(), invitationMessage(message.message(), firstUser));
    }

    private static String invitationMessage(String message, UserAccount firstUser) {
        return "User %s invite you for partnership {%s}.".formatted(firstUser.getUsername().username(), message);
    }

    private static String successfullyAddedPartnershipMessage(UserAccount firstUser, UserAccount secondUser) {
        return "Partnership {%s - %s} successfully added.".formatted(firstUser.getUsername().username(), secondUser.getUsername().username());
    }
}
