package core.project.messaging.application.service;

import core.project.messaging.application.dto.messaging.Message;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.enumerations.InvitationResult;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.services.PartnershipsService;
import core.project.messaging.domain.user.value_objects.PartnershipInvitation;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.SessionStorage;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Objects;
import java.util.Optional;

import static core.project.messaging.application.util.WSUtilities.closeSession;
import static core.project.messaging.application.util.WSUtilities.sendMessage;

@ApplicationScoped
public class UserSessionService {

    private final SessionStorage sessionStorage;

    private final PartnershipsService partnershipsService;

    private final OutboundUserRepository outboundUserRepository;

    UserSessionService(SessionStorage sessionStorage,
                       PartnershipsService partnershipsService,
                       OutboundUserRepository outboundUserRepository) {
        this.sessionStorage = sessionStorage;
        this.partnershipsService = partnershipsService;
        this.outboundUserRepository = outboundUserRepository;
    }

    public void onOpen(Session session, Username username) {
        Result<User, Throwable> account = outboundUserRepository.findByUsername(username);
        if (!account.success()) {
            closeSession(session, Message.error("This account does`t exist."));
            return;
        }
        if (sessionStorage.contains(username)) {
            closeSession(session, Message.error("You can`t duplicate sessions."));
            return;
        }

        sessionStorage.put(session, Objects.requireNonNull(account.orElseThrow()));

        sendMessage(session, Message.info("Successful connection to messaging"));
        partnershipsService
                .getAll(username.username())
                .forEach((user, message) ->
                        sendMessage(session, Message.partnershipRequest(String.format("%s: {%s}", user, message), user)));
    }

    public void onMessage(Session session, Username username, Message message) {
        Log.infof("Handling %s of user -> %s.", message.type(), username.username());

        Optional<User> userAccount = extractAccount(session);
        if (userAccount.isEmpty()) {
            String errorMessage = "Unexpected error. Session does not have a user account.";
            Log.error(errorMessage);
            closeSession(session, Message.error(errorMessage));
            return;
        }

        handleMessage(message, session, userAccount.orElseThrow());
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
                closeSession(session, Message.error("Unexpected error. The connected web socket connection is not in the storage."));
                return;
            }

            Optional<User> addresseeAccount = extractAccount(addresseeSession.orElseThrow());
            if (addresseeAccount.isEmpty()) {
                closeSession(addresseeSession.get(), Message
                        .error("Unexpected error. The connected web socket connection is not in the storage."));
                return;
            }

            var invitationResult = partnershipsService.partnershipRequest(addresser, addresseeAccount.get(), message.message());
            if (!invitationResult.success()) {
                sendMessage(session, Message.error(invitationResult.throwable().getMessage()));
                return;
            }

            sendPartnershipInvitationResult(session, addressee, invitationResult, addresseeSession.get());
            return;
        }

        var invitationResult = partnershipsService.partnershipRequest(addresser, addressee, message.message());
        if (!invitationResult.success()) {
            sendMessage(session, Message.error(invitationResult.throwable().getMessage()));
            return;
        }

        sendMessage(session, Message.userInfo(invitationResult.value().message()));
    }

    public void onClose(Session session, Username username) {
        sessionStorage.remove(username);
    }

    public Optional<User> extractAccount(Session session) {
        return Optional.ofNullable(session.getUserProperties().get(SessionStorage.SessionProperties.USER_ACCOUNT.key()))
                .filter(User.class::isInstance)
                .map(User.class::cast);
    }

    private static void sendPartnershipInvitationResult(Session session, Username addressee,
                                                        Result<PartnershipInvitation, Throwable> invitationResult,
                                                        Session addresseeSession) {

        InvitationResult result = invitationResult.value().result();
        String rawMessage = invitationResult.value().message();
        if (result == InvitationResult.BOTH) {
            Message message = Message.userInfo(rawMessage);
            sendMessage(session, message);
            sendMessage(addresseeSession, message);
            return;
        }

        if (result == InvitationResult.ADDRESSEE) {
            Message message = Message.partnershipRequest(rawMessage, addressee.username());
            sendMessage(addresseeSession, message);
        }
    }
}
