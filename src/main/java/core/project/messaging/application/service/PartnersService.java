package core.project.messaging.application.service;

import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.services.PartnershipsService;
import core.project.messaging.domain.user.value_objects.Username;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import static core.project.messaging.application.util.JsonUtilities.responseException;

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
            throw responseException(Response.Status.BAD_REQUEST, "Invalid username");
        }

        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);
        return outboundUserRepository.listOfPartners(username, limit, offSet)
                .orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "User does not exist.\uD83D\uDC7B"));
    }

    public void removePartner(String username, String partner) {
        try {
            partnershipsService.removePartner(username, partner);
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
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
