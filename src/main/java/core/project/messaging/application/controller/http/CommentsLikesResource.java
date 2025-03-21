package core.project.messaging.application.controller.http;

import core.project.messaging.domain.articles.services.CommentsService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static core.project.messaging.application.controller.http.ArticlesResource.nonNull;

@Authenticated
@Path("/comments/likes")
public class CommentsLikesResource {

    private final JsonWebToken jwt;

    private final CommentsService commentsService;

    CommentsLikesResource(JsonWebToken jwt, CommentsService commentsService) {
        this.jwt = jwt;
        this.commentsService = commentsService;
    }

    @POST
    @Path("/like-comment")
    public Response likeArticle(@QueryParam("commentID") String commentID) {
        nonNull(commentID);
        commentsService.like(commentID, jwt.getName());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/remove-like")
    public Response removeLike(@QueryParam("commentID") String commentID) {
        nonNull(commentID);
        commentsService.deleteLike(commentID, jwt.getName());
        return Response.accepted().build();
    }
}
