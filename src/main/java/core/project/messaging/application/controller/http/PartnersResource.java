package core.project.messaging.application.controller.http;

import core.project.messaging.application.service.PartnersService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static core.project.messaging.application.controller.http.ArticlesResource.nonNull;

@Authenticated
@Path("/account")
public class PartnersResource {

    private final JsonWebToken jwt;

    private final PartnersService partnersService;

    PartnersResource(JsonWebToken jwt, PartnersService partnersService) {
        this.jwt = jwt;
        this.partnersService = partnersService;
    }

    @GET
    @Path("/partners")
    public Response partners(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(partnersService.listOfPartners(jwt.getName(), pageNumber, pageSize)).build();
    }

    @DELETE
    @Path("/remove-partner")
    public Response removePartner(@QueryParam("partner") String partner) {
        nonNull(partner);
        partnersService.removePartner(jwt.getName(), partner);
        return Response.noContent().build();
    }
}
