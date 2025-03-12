package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.service.ArticlesApplicationService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/articles")
public class ArticlesResource {

    private final JsonWebToken jwt;

    private final ArticlesApplicationService articlesService;

    ArticlesResource(JsonWebToken jwt, ArticlesApplicationService articlesService) {
        this.jwt = jwt;
        this.articlesService = articlesService;
    }

    @POST
    @Path("/post")
    public Response create(ArticleForm articleForm) {
        articlesService.save(articleForm, jwt.getName());
        return Response.ok().build();
    }

    @GET
    @Path("/viewArticle")
    public Response viewArticle(@QueryParam("id") String id) {
        return Response.ok(articlesService.viewArticle(id, jwt.getName())).build();
    }
}
