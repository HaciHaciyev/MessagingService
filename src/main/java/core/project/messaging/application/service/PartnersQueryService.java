package core.project.messaging.application.service;

import core.project.messaging.domain.user.entities.UserAccount;
import core.project.messaging.domain.user.value_objects.Username;
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

    public Object listOfPartners(String username, int pageNumber, int pageSize) {
        if (!Username.validate(username)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid username").build());
        }
        if (pageNumber < 0) {
            pageNumber = 0;
        }

        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);
        return outboundUserRepository
                .listOfPartners(username, limit, offSet)
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

    public static int buildLimit(Integer pageSize) {
        int limit;
        if (pageSize > 0 && pageSize <= 25) {
            limit = pageSize;
        } else {
            limit = 10;
        }
        return limit;
    }

    public static int buildOffSet(Integer limit, Integer pageNumber) {
        int offSet;
        if (limit > 0 && pageNumber > 0) {
            offSet = (pageNumber - 1) * limit;
        } else {
            offSet = 0;
        }
        return offSet;
    }
}
