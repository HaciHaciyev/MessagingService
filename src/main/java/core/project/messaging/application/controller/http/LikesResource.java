package core.project.messaging.application.controller.http;

import core.project.messaging.domain.articles.services.ArticlesService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static core.project.messaging.application.controller.http.ArticlesResource.nonNull;

@Authenticated
@Path("/articles/likes")
public class LikesResource {

    private final JsonWebToken jwt;

    private final ArticlesService articlesService;

    LikesResource(JsonWebToken jwt, ArticlesService articlesService) {
        this.jwt = jwt;
        this.articlesService = articlesService;
    }

    @POST
    @Path("/like-article")
    public Response likeArticle(@QueryParam("articleID") String articleID) {
        nonNull(articleID);
        articlesService.likeArticle(articleID, jwt.getName());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/remove-like")
    public Response removeLike(@QueryParam("articleID") String articleID) {
        nonNull(articleID);
        articlesService.deleteLike(articleID, jwt.getName());
        return Response.accepted().build();
    }
}
