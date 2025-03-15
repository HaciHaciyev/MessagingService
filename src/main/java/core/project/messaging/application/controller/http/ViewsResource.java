package core.project.messaging.application.controller.http;

import core.project.messaging.domain.articles.services.ArticlesService;
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

    private final ArticlesService articlesApplicationService;

    ViewsResource(JsonWebToken jwt, ArticlesService articlesService) {
        this.jwt = jwt;
        this.articlesApplicationService = articlesService;
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("articleID") String articleID) {
        articlesApplicationService.deleteView(articleID, jwt.getName());
        return Response.noContent().build();
    }
}
