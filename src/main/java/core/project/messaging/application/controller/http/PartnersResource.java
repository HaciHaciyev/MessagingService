package core.project.messaging.application.controller.http;

import core.project.messaging.application.service.PartnersQueryService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/account")
public class PartnersResource {

    private final JsonWebToken jwt;

    private final PartnersQueryService partnersQueryService;

    PartnersResource(JsonWebToken jwt, PartnersQueryService partnersQueryService) {
        this.jwt = jwt;
        this.partnersQueryService = partnersQueryService;
    }

    @GET
    @Path("/partners")
    public Response partners(@QueryParam("pageNumber") int pageNumber) {
        return Response.ok(partnersQueryService.listOfPartners(jwt.getName(), pageNumber)).build();
    }

    @DELETE
    @Path("/remove-partner")
    public Response removePartner(@QueryParam("partner") String partner) {
        partnersQueryService.removePartner(jwt.getName(), partner);
        return Response.noContent().build();
    }
}
