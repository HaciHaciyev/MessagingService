package core.project.messaging.application.service;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.cache.PartnershipRequestsService;
import core.project.messaging.infrastructure.repository.inbound.InboundUserRepository;
import core.project.messaging.infrastructure.repository.outbound.OutboundUserRepository;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import core.project.messaging.infrastructure.utilities.containers.StatusPair;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
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

    private final PartnershipRequestsService partnershipRequestsService;

    private static final ConcurrentHashMap<Username, Pair<Session, UserAccount>> sessions = new ConcurrentHashMap<>();

    public void handleOnOpen(Session session, Username username) {
        CompletableFuture.runAsync(() -> outboundUserRepository.findByUsername(username).handle(
                userAccount -> sessions.put(username, Pair.of(session, userAccount)),
                throwable -> sendMessage(session, Message.error("This account is do not founded.")))
        ).thenRun(() -> messages(session, username));
    }

    private void messages(Session session, Username username) {
        partnershipRequestsService
                .getAll(username.username())
                .forEach((user, message) -> {
                    Log.info(user + message);
                    sendMessage(session, Message.info(String.format("%s: {%s}", user, message)));
                });
    }

    public void handleOnMessage(Session session, Username username, Message message) {
        Log.infof("Sessions -> %s", sessions.keySet());
        final Pair<Session, UserAccount> sessionUser = Pair.of(session, sessions.get(username).getSecond());

        Log.infof("Handling %s of user -> %s", message.type(), username.username());
        CompletableFuture.runAsync(() -> handleWebSocketMessage(message, sessionUser.getFirst(), sessionUser.getSecond()));
    }

    private void handleWebSocketMessage(Message message, Session session, UserAccount user) {
        if (message.type().equals(PARTNERSHIP_REQUEST)) {
            String addressee = message.partner();
            if (addressee == null  || addressee.isEmpty()) {
                sendMessage(session, Message.error("Invalid partner username."));
                return;
            }

            partnershipRequest(session, user, message, new Username(addressee));
            return;
        }

        sendMessage(session, Message.error("Invalid message type."));
    }

    private void partnershipRequest(Session session, UserAccount addresser, Message message, Username addressee) {
        if (sessions.containsKey(addressee)) {
            processPartnershipRequest(Pair.of(session, addresser), sessions.get(addressee), message);
            return;
        }

        processPartnershipRequest(Pair.of(session, addresser), addressee, message);
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

            final Message messageOfResult = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            sendMessage(addresserPair.getFirst(), messageOfResult);
            sendMessage(addresseePair.getFirst(), messageOfResult);

            partnershipRequestsService.delete(addressee, addresser);
            partnershipRequestsService.delete(addresser, addressee);
            return;
        }

        sendMessage(addresseePair.getFirst(), invitationMessage(message.message(), addresserAccount));
        sendMessage(addresserPair.getFirst(), Message.userInfo(String.format("Wait for user {%s} answer.", addressee)));
    }

    private void processPartnershipRequest(final Pair<Session, UserAccount> addresserPair,
                                           final Username addressee,
                                           final Message message) {

        final Result<UserAccount, Throwable> result = outboundUserRepository.findByUsername(addressee);
        if (!result.success()) {
            sendMessage(addresserPair.getFirst(), Message.error("This account is not exists."));
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

            final Message messageOfResult = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            sendMessage(addresserPair.getFirst(), messageOfResult);
            partnershipRequestsService.put(addressee.username(), addresser, message.message());

            partnershipRequestsService.delete(addressee.username(), addresser);
            partnershipRequestsService.delete(addresser, addressee.username());
            return;
        }

        sendMessage(addresserPair.getFirst(), Message.userInfo(String.format("Wait for user {%s} answer.", addressee.username())));
    }

    private StatusPair<String> isPartnershipCreated(String addresser, String addressee) {
        final Map<String, String> requests = partnershipRequestsService.getAll(addresser);
        if (requests.containsKey(addressee)) {
            return StatusPair.ofTrue(requests.get(addressee));
        }

        return StatusPair.ofFalse();
    }

    private static Message invitationMessage(String message, UserAccount addresser) {
        return Message.userInfo("User {%s} invite you for partnership {%s}.".formatted(addresser.getUsername().username(), message));
    }

    private static Message successfullyAddedPartnershipMessage(UserAccount firstUser, UserAccount secondUser) {
        return Message.userInfo("Partnership {%s - %s} successfully added.".formatted(firstUser.getUsername().username(), secondUser.getUsername().username()));
    }

    public void handleOnClose(Session session, Username username) {
        sessions.remove(username);
        closeSession(session, "Session is closed.");
    }
}
