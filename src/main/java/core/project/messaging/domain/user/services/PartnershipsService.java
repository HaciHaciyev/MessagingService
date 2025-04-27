package core.project.messaging.domain.user.services;

import core.project.messaging.application.dto.Message;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.commons.containers.StatusPair;
import core.project.messaging.domain.commons.tuples.Pair;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.enumerations.MessageAddressee;
import core.project.messaging.domain.user.repositories.InboundUserRepository;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.cache.PartnershipRequestsRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class PartnershipsService {

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    private final PartnershipRequestsRepository requestsRepository;

    PartnershipsService(InboundUserRepository inboundUserRepository,
                        OutboundUserRepository outboundUserRepository,
                        PartnershipRequestsRepository requestsRepository) {
        this.inboundUserRepository = inboundUserRepository;
        this.outboundUserRepository = outboundUserRepository;
        this.requestsRepository = requestsRepository;
    }

    public Map<String, String> getAll(String username) {
        return requestsRepository.getAll(username);
    }

    public Pair<MessageAddressee, Message> partnershipRequest(
            final User addresserAccount,
            final User addresseeAccount,
            final Message message) {

        final String addresser = addresserAccount.username().username();
        final String addressee = addresseeAccount.username().username();

        if (addresser.equals(addressee))
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                    Message.error("You cannot request yourself for partnership."));

        final boolean isRequestRetried = requestsRepository.get(addressee, addresser).status();
        if (isRequestRetried)
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                Message.error("You cannot send a repeat partnership request to a user while the previous one is active."));

        final boolean isAlreadyHavePartnership = outboundUserRepository.havePartnership(addresserAccount, addresseeAccount);
        if (isAlreadyHavePartnership)
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                Message.error("You can`t invite someone who has partnership with you already."));

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

    public Pair<MessageAddressee, Message> partnershipRequest(
            final User addresserAccount,
            final Username addressee,
            final Message message) {

        final String addresser = addresserAccount.username().username();

        final Result<User, Throwable> result = outboundUserRepository.findByUsername(addressee.username());
        if (!result.success()) {
            Message errorMessage = Message.error("This account is not exists.");
            return Pair.of(MessageAddressee.ONLY_ADDRESSER, errorMessage);
        }

        final User addresseeAccount = result.value();

        if (addresser.equals(addressee.username()))
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                    Message.error("You cannot request yourself for partnership."));

        final boolean isRequestRetried = requestsRepository.get(addressee.username(), addresser).status();
        if (isRequestRetried)
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                Message.error("You cannot send a repeat partnership request to a user while the previous one is active."));

        final boolean isAlreadyHavePartnership = outboundUserRepository.havePartnership(addresserAccount, addresseeAccount);
        if (isAlreadyHavePartnership)
            return Pair.of(MessageAddressee.ONLY_ADDRESSER,
                Message.error("You can`t invite someone who has partnership with you already."));

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

    public void partnershipDecline(final User user, final Username partner) {
        requestsRepository.delete(user.username().username(), partner.username());
    }

    public void removePartner(String username, String partner) {
        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User does`t exists."));

        User partnerAccount = outboundUserRepository
                .findByUsername(partner)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist."));

        if (!outboundUserRepository.havePartnership(user, partnerAccount)) {
            throw new IllegalArgumentException("This partnership not exists.");
        }

        inboundUserRepository.removePartnership(user, partnerAccount);
    }

    private StatusPair<String> isPartnershipCreated(String addresser, String addressee) {
        final Map<String, String> requests = getAll(addresser);
        return requests.containsKey(addressee) ? StatusPair.ofTrue(requests.get(addressee)) : StatusPair.ofFalse();
    }

    private static Message successfullyAddedPartnershipMessage(User firstUser, User secondUser) {
        return Message.userInfo("Partnership {%s - %s} successfully added."
                .formatted(firstUser.username().username(), secondUser.username().username()));
    }

    private static Message invitationMessage(String message, User addresser) {
        String partner = addresser.username().username();
        return Message.partnershipRequest("User {%s} invite you for partnership {%s}.".formatted(partner, message), partner);
    }
}
