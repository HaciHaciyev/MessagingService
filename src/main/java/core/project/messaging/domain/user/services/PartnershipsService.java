package core.project.messaging.domain.user.services;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.user.entities.UserAccount;
import core.project.messaging.domain.user.enumerations.MessageAddressee;
import core.project.messaging.domain.user.repositories.InboundUserRepository;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.PartnershipRequestsRepository;
import core.project.messaging.infrastructure.utilities.containers.Pair;
import core.project.messaging.infrastructure.utilities.containers.Result;
import core.project.messaging.infrastructure.utilities.containers.StatusPair;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class PartnershipsService {

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    private final PartnershipRequestsRepository requestsRepository;

    PartnershipsService(InboundUserRepository inboundUserRepository, OutboundUserRepository outboundUserRepository, PartnershipRequestsRepository requestsRepository) {
        this.inboundUserRepository = inboundUserRepository;
        this.outboundUserRepository = outboundUserRepository;
        this.requestsRepository = requestsRepository;
    }

    public Map<String, String> getAll(String username) {
        return requestsRepository.getAll(username);
    }

    public Pair<MessageAddressee, Message> partnershipRequest(final UserAccount addresserAccount, final UserAccount addresseeAccount, final Message message) {
        final String addresser = addresserAccount.getUsername().username();
        final String addressee = addresseeAccount.getUsername().username();

        final boolean isRequestRetried = requestsRepository.get(addressee, addresser).status();
        if (isRequestRetried) {
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                    Message.error("You cannot send a repeat partnership request to a user while the previous one is active."));
        }

        final boolean isAlreadyHavePartnership = outboundUserRepository.havePartnership(addresserAccount, addresseeAccount);
        if (isAlreadyHavePartnership) {
            return Pair.of(MessageAddressee.ONLY_ADDRESSER, Message.error("You can`t invite someone who has partnership with you already."));
        }

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

    public Pair<MessageAddressee, Message> partnershipRequest(final UserAccount addresserAccount, final Username addressee, final Message message) {
        final String addresser = addresserAccount.getUsername().username();

        final Result<UserAccount, Throwable> result = outboundUserRepository.findByUsername(addressee.username());
        if (!result.success()) {
            Message errorMessage = Message.error("This account is not exists.");
            return Pair.of(MessageAddressee.ONLY_ADDRESSER, errorMessage);
        }

        final UserAccount addresseeAccount = result.value();

        final boolean isRequestRetried = requestsRepository.get(addressee.username(), addresser).status();
        if (isRequestRetried) {
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                    Message.error("You cannot send a repeat partnership request to a user while the previous one is active."));
        }

        final boolean isAlreadyHavePartnership = outboundUserRepository.havePartnership(addresserAccount, addresseeAccount);
        if (isAlreadyHavePartnership) {
            return Pair.of(MessageAddressee.ONLY_ADDRESSER, Message.error("You can`t invite someone who has partnership with you already."));
        }

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

    public void partnershipDecline(final UserAccount user, final Username partner) {
        requestsRepository.delete(user.getUsername().username(), partner.username());
    }

    public void removePartner(String username, String partner) {
        UserAccount userAccount = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User does`t exists."));

        UserAccount partnerAccount = outboundUserRepository
                .findByUsername(partner)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist."));

        if (!outboundUserRepository.havePartnership(userAccount, partnerAccount)) {
            throw new IllegalArgumentException("This partnership not exists.");
        }

        inboundUserRepository.removePartnership(userAccount, partnerAccount);
    }

    private StatusPair<String> isPartnershipCreated(String addresser, String addressee) {
        final Map<String, String> requests = getAll(addresser);
        return requests.containsKey(addressee) ? StatusPair.ofTrue(requests.get(addressee)) : StatusPair.ofFalse();
    }

    private static Message successfullyAddedPartnershipMessage(UserAccount firstUser, UserAccount secondUser) {
        return Message.userInfo("Partnership {%s - %s} successfully added.".formatted(firstUser.getUsername().username(), secondUser.getUsername().username()));
    }

    private static Message invitationMessage(String message, UserAccount addresser) {
        String partner = addresser.getUsername().username();
        return Message.partnershipRequest("User {%s} invite you for partnership {%s}.".formatted(partner, message), partner);
    }
}
