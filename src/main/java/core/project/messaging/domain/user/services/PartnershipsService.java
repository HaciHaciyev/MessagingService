package core.project.messaging.domain.user.services;

import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.commons.containers.StatusPair;
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.enumerations.InvitationResult;
import core.project.messaging.domain.user.repositories.InboundUserRepository;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.repositories.PartnershipRequestsRepository;
import core.project.messaging.domain.user.value_objects.PartnershipInvitation;
import core.project.messaging.domain.user.value_objects.Username;
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
        return requestsRepository.getAll(new Username(username));
    }

    public Result<PartnershipInvitation, Throwable> partnershipRequest(
            final User addresserAccount,
            final User addresseeAccount,
            final String message) {

        final String addresser = addresserAccount.username().username();
        final String addressee = addresseeAccount.username().username();

        if (addresser.equals(addressee))
            return Result.failure(new IllegalDomainArgumentException("You cannot request yourself for partnership."));

        Username addresseeUsername = new Username(addressee);
        Username addresserUsername = new Username(addresser);

        final boolean isRequestRetried = requestsRepository.get(addresseeUsername, addresserUsername).status();
        if (isRequestRetried)
            return Result.failure(new IllegalDomainArgumentException(
                    "You cannot send a repeat partnership request to a user while the previous one is active."));

        final boolean isAlreadyHavePartnership = outboundUserRepository.havePartnership(addresserAccount, addresseeAccount);
        if (isAlreadyHavePartnership)
            return Result.failure(
                    new IllegalDomainArgumentException("You can`t invite someone who has partnership with you already."));

        requestsRepository.put(addresseeUsername, addresserUsername, message);

        final StatusPair<String> isPartnershipCreated = isPartnershipCreated(addresser, addressee);
        if (isPartnershipCreated.status()) {
            addresserAccount.addPartner(addresseeAccount);
            addresseeAccount.addPartner(addresserAccount);
            inboundUserRepository.addPartnership(addresseeAccount, addresserAccount);

            requestsRepository.delete(addresseeUsername, addresserUsername);
            requestsRepository.delete(addresserUsername, addresseeUsername);

            return Result.success(new PartnershipInvitation(
                    InvitationResult.BOTH, successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount)));
        }

        return Result.success(new PartnershipInvitation(InvitationResult.ADDRESSEE, invitationMessage(message, addresserAccount)));
    }

    public Result<PartnershipInvitation, Throwable> partnershipRequest(
            final User addresserAccount,
            final Username addressee,
            final String message) {

        final String addresser = addresserAccount.username().username();

        final Result<User, Throwable> result = outboundUserRepository.findByUsername(addressee);
        if (!result.success())
            return Result.failure(new IllegalDomainArgumentException("This account is not exists."));

        final User addresseeAccount = result.value();

        if (addresser.equals(addressee.username()))
            return Result.failure(new IllegalDomainArgumentException("You cannot request yourself for partnership."));

        Username addresserUsername = new Username(addresser);
        final boolean isRequestRetried = requestsRepository.get(addressee, addresserUsername).status();
        if (isRequestRetried)
            return Result.failure(
                    new IllegalDomainArgumentException("You can`t invite someone who has partnership with you already."));

        final boolean isAlreadyHavePartnership = outboundUserRepository.havePartnership(addresserAccount, addresseeAccount);
        if (isAlreadyHavePartnership)
            return Result.failure(
                    new IllegalDomainArgumentException("You can`t invite someone who has partnership with you already."));

        requestsRepository.put(addressee, addresserUsername, message);

        final StatusPair<String> isPartnershipCreated = isPartnershipCreated(addresser, addressee.username());
        if (isPartnershipCreated.status()) {
            addresserAccount.addPartner(addresseeAccount);
            addresseeAccount.addPartner(addresserAccount);
            inboundUserRepository.addPartnership(addresseeAccount, addresserAccount);

            requestsRepository.delete(addressee, addresserUsername);
            requestsRepository.delete(addresserUsername, addressee);

            String successMessage = successfullyAddedPartnershipMessage(addresserAccount, addresseeAccount);
            requestsRepository.put(addressee, addresserUsername, successMessage);
            return Result.success(new PartnershipInvitation(InvitationResult.ADDRESSER, successMessage));
        }

        return Result.success(new PartnershipInvitation(InvitationResult.ADDRESSER, waitForInvitation(addressee)));
    }

    public void partnershipDecline(final User user, final Username partner) {
        requestsRepository.delete(user.username(), partner);
    }

    public void removePartner(String username, String partner) {
        User user = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalDomainArgumentException("User does`t exists."));

        User partnerAccount = outboundUserRepository
                .findByUsername(new Username(partner))
                .orElseThrow(() -> new IllegalDomainArgumentException("User does not exist."));

        if (!outboundUserRepository.havePartnership(user, partnerAccount)) {
            throw new IllegalDomainArgumentException("This partnership not exists.");
        }

        inboundUserRepository.removePartnership(user, partnerAccount);
    }

    private StatusPair<String> isPartnershipCreated(String addresser, String addressee) {
        final Map<String, String> requests = getAll(addresser);
        return requests.containsKey(addressee) ? StatusPair.ofTrue(requests.get(addressee)) : StatusPair.ofFalse();
    }

    private static String successfullyAddedPartnershipMessage(User firstUser, User secondUser) {
        return "Partnership {%s - %s} successfully added."
                .formatted(firstUser.username().username(), secondUser.username().username());
    }

    private static String invitationMessage(String message, User addresser) {
        String partner = addresser.username().username();
        return "User {%s} invite you for partnership {%s}.".formatted(partner, message);
    }

    private static String waitForInvitation(Username addressee) {
        return String.format("Wait for user {%s} answer.", addressee.username());
    }
}
