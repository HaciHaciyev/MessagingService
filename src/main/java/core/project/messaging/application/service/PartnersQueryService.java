package core.project.messaging.application.service;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.dal.repository.inbound.InboundUserRepository;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PartnersQueryService {

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    PartnersQueryService(InboundUserRepository inboundUserRepository, OutboundUserRepository outboundUserRepository) {
        this.inboundUserRepository = inboundUserRepository;
        this.outboundUserRepository = outboundUserRepository;
    }

    public Object listOfPartners(String username, int pageNumber) {
        if (!Username.validate(username)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid username").build());
        }
        if (pageNumber < 0) {
            pageNumber = 0;
        }

        return outboundUserRepository
                .listOfPartners(username, pageNumber)
                .orElseThrow(
                        () -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.\uD83D\uDC7B").build())
                );
    }

    public void removePartner(String username, String partner) {
        if (!Username.validate(partner) || !Username.validate(username)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid partner username.").build());
        }

        UserAccount userAccount = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(
                        () -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build())
                );

        Username partnerUsername = new Username(partner);
        UserAccount partnerAccount = outboundUserRepository
                .findByUsername(partnerUsername)
                .orElseThrow(
                        () -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build())
                );

        if (!outboundUserRepository.havePartnership(userAccount, partnerAccount)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("This partnership not exists.").build());
        }

        inboundUserRepository.removePartnership(userAccount, partnerAccount);
    }
}
