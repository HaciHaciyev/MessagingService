package core.project.messaging.application.controller.http;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.dal.repository.inbound.InboundUserRepository;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Authenticated
@Path("/account")
public class PartnersResource {

    private final JsonWebToken jwt;

    private final InboundUserRepository inboundUserRepository;

    private final OutboundUserRepository outboundUserRepository;

    public PartnersResource(JsonWebToken jwt, OutboundUserRepository outboundUserRepository, InboundUserRepository inboundUserRepository) {
        this.jwt = jwt;
        this.outboundUserRepository = outboundUserRepository;
        this.inboundUserRepository = inboundUserRepository;
    }

    @GET
    @Path("/partners")
    public Response partners(@QueryParam("pageNumber") int pageNumber) {
        List<String> partnersUsernames = outboundUserRepository
                .listOfPartners(jwt.getName(), pageNumber)
                .orElseThrow(
                        () -> new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.\uD83D\uDC7B").build())
                );

        return Response.ok(partnersUsernames).build();
    }

    @DELETE
    @Path("/remove-partner")
    public Response removePartner(@QueryParam("partner") String partner) {
        if (!Username.validate(partner)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid partner username.").build());
        }

        UserAccount userAccount = outboundUserRepository
                .findByUsername(new Username(jwt.getName()))
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
        return Response.noContent().build();
    }
}
