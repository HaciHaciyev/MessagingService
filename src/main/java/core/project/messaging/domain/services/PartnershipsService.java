package core.project.messaging.domain.services;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.enumerations.MessageAddressee;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.PartnershipRequestsRepository;
import core.project.messaging.infrastructure.dal.repository.inbound.InboundUserRepository;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import core.project.messaging.infrastructure.utilities.containers.StatusPair;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class PartnershipsService {

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    private final PartnershipRequestsRepository requestsRepository;

    public Map<String, String> getAll(String username) {
        return requestsRepository.getAll(username);
    }

    public Pair<MessageAddressee, Message> partnershipRequest(final Pair<Session, UserAccount> addresserPair,
                                                              final Pair<Session, UserAccount> addresseePair,
                                                              final Message message) {

        final UserAccount addresserAccount = addresserPair.getSecond();
        final String addresser = addresserAccount.getUsername().username();

        final UserAccount addresseeAccount = addresseePair.getSecond();
        final String addressee = addresseeAccount.getUsername().username();

        requestsRepository.put(addressee, addresser, message.message());

        final StatusPair<String> isPartnershipCreated = isPartnershipCreated(addresser, addressee);
        if (isPartnershipCreated.status()) {
            addresserAccount.addPartner(addresseeAccount);
            addresseeAccount.addPartner(addresserAccount);
            inboundUserRepository.addPartnership(addresseeAccount, addresserAccount);

            requestsRepository.delete(addressee, addresser);
            requestsRepository.delete(addresser, addressee);

            final Message messageOfResult = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            return Pair.of(MessageAddressee.FOR_ALL, messageOfResult);
        }

        return Pair.of(MessageAddressee.ONLY_ADDRESSEE, invitationMessage(message.message(), addresserAccount));
    }

    public Pair<MessageAddressee, Message> partnershipRequest(final Pair<Session, UserAccount> addresserPair,
                                                              final Username addressee,
                                                              final Message message) {

        final UserAccount addresserAccount = addresserPair.getSecond();
        final String addresser = addresserAccount.getUsername().username();

        final Result<UserAccount, Throwable> result = outboundUserRepository.findByUsername(addressee);
        if (!result.success()) {
            Message errorMessage = Message.error("This account is not exists.");
            return Pair.of(MessageAddressee.ONLY_ADDRESSER, errorMessage);
        }

        final UserAccount addresseeAccount = result.value();

        requestsRepository.put(addressee.username(), addresser, message.message());

        final StatusPair<String> isPartnershipCreated = isPartnershipCreated(addresser, addressee.username());
        if (isPartnershipCreated.status()) {
            addresserAccount.addPartner(addresseeAccount);
            addresseeAccount.addPartner(addresserAccount);
            inboundUserRepository.addPartnership(addresseeAccount, addresserAccount);

            requestsRepository.delete(addressee.username(), addresser);
            requestsRepository.delete(addresser, addressee.username());

            final Message messageOfResult = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            requestsRepository.put(addressee.username(), addresser, messageOfResult.message());
            return Pair.of(MessageAddressee.ONLY_ADDRESSER, messageOfResult);
        }

        Message messageOfResult = Message.userInfo(String.format("Wait for user {%s} answer.", addressee.username()));
        return Pair.of(MessageAddressee.ONLY_ADDRESSER, messageOfResult);
    }

    private StatusPair<String> isPartnershipCreated(String addresser, String addressee) {
        final Map<String, String> requests = getAll(addresser);
        return requests.containsKey(addressee) ? StatusPair.ofTrue(requests.get(addressee)) : StatusPair.ofFalse();
    }

    private static Message successfullyAddedPartnershipMessage(UserAccount firstUser, UserAccount secondUser) {
        return Message.userInfo("Partnership {%s - %s} successfully added.".formatted(firstUser.getUsername().username(), secondUser.getUsername().username()));
    }

    private static Message invitationMessage(String message, UserAccount addresser) {
        return Message.userInfo("User {%s} invite you for partnership {%s}.".formatted(addresser.getUsername().username(), message));
    }
}
