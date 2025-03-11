package core.project.messaging.application.controller.http;

import core.project.messaging.application.service.ArticlesApplicationService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/articles/views")
public class ViewsResource {

    private final JsonWebToken jwt;

    private final ArticlesApplicationService articlesApplicationService;

    ViewsResource(JsonWebToken jwt, ArticlesApplicationService articlesApplicationService) {
        this.jwt = jwt;
        this.articlesApplicationService = articlesApplicationService;
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("articleID") String articleID) {
        articlesApplicationService.deleteView(articleID, jwt.getName());
        return Response.noContent().build();
    }
}
