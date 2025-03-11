package core.project.messaging.application.controller.http;

import core.project.messaging.application.service.ArticlesApplicationService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/articles/likes")
public class LikesResource {

    private final JsonWebToken jwt;

    private final ArticlesApplicationService articlesApplicationService;

    LikesResource(JsonWebToken jwt, ArticlesApplicationService articlesApplicationService) {
        this.jwt = jwt;
        this.articlesApplicationService = articlesApplicationService;
    }

    @POST
    @Path("/like-article")
    public Response likeArticle(@QueryParam("articleID") String articleID) {
        articlesApplicationService.likeArticle(articleID, jwt.getName());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/remove-like")
    public Response removeLike(@QueryParam("articleID") String articleID) {
        articlesApplicationService.deleteLike(articleID, jwt.getName());
        return Response.accepted().build();
    }
}
