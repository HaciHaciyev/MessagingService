package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.service.ArticlesQueryService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/articles")
public class ArticlesResource {

    private final JsonWebToken jwt;

    private final ArticlesQueryService articlesQueryService;

    ArticlesResource(JsonWebToken jwt, ArticlesQueryService articlesQueryService) {
        this.jwt = jwt;
        this.articlesQueryService = articlesQueryService;
    }

    @POST
    @Path("/post")
    public Response create(ArticleForm articleForm) {
        articlesQueryService.save(articleForm, jwt.getName());
        return Response.ok().build();
    }

    @GET
    @Path("/findByID")
    public Response findByID(@QueryParam("id") String id) {
        return Response.ok(articlesQueryService.findByID(id, jwt.getName())).build();
    }
}
