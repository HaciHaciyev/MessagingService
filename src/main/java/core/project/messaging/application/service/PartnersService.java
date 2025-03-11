package core.project.messaging.application.service;

import core.project.messaging.domain.user.services.PartnershipsService;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PartnersService {

    private final PartnershipsService partnershipsService;

    private final OutboundUserRepository outboundUserRepository;

    PartnersService(PartnershipsService partnershipsService, OutboundUserRepository outboundUserRepository) {
        this.partnershipsService = partnershipsService;
        this.outboundUserRepository = outboundUserRepository;
    }

    public Object listOfPartners(String username, int pageNumber, int pageSize) {
        if (!Username.validate(username)) {
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid username")
                    .build());
        }

        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);
        return outboundUserRepository
                .listOfPartners(username, limit, offSet)
                .orElseThrow(
                        () -> new WebApplicationException(Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity("User does not exist.\uD83D\uDC7B")
                                .build())
                );
    }

    public void removePartner(String username, String partner) {
        try {
            partnershipsService.removePartner(username, partner);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build());
        }
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
