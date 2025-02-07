package core.project.messaging.application.controller.http;

import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Authenticated
@Path("/account")
public class PartnersResource {

    private final JsonWebToken jwt;

    private final OutboundUserRepository outboundUserRepository;

    public PartnersResource(JsonWebToken jwt, OutboundUserRepository outboundUserRepository) {
        this.jwt = jwt;
        this.outboundUserRepository = outboundUserRepository;
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
}
