package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.CommentForm;
import core.project.messaging.application.service.ArticlesApplicationService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/articles/comments")
public class CommentsResource {

    private final JsonWebToken jwt;

    private final ArticlesApplicationService articlesApplicationService;

    CommentsResource(JsonWebToken jwt, ArticlesApplicationService articlesApplicationService) {
        this.jwt = jwt;
        this.articlesApplicationService = articlesApplicationService;
    }

    @POST
    @Path("/create")
    public Response create(CommentForm commentForm) {
        articlesApplicationService.createComment(commentForm, jwt.getName());
        return Response.accepted().build();
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("commentID") String commentID) {
        articlesApplicationService.deleteComment(commentID, jwt.getName());
        return Response.accepted().build();
    }

    /*@GET
    @Path("/page")
    public Response commentPageOf(@QueryParam("articleID") String articleID,
                                  @QueryParam("pageNumber") String pageNumber,
                                  @QueryParam("pageSize") String pageSize) {

        String username = jwt.getName();
        // TODO
        return null;
    }

    @GET
    @Path("/page")
    public Response commentPageOf(@QueryParam("articleID") String articleID,
                                  @QueryParam("parentCommentID") String parentCommentID,
                                  @QueryParam("pageNumber") String pageNumber,
                                  @QueryParam("pageSize") String pageSize) {

        String username = jwt.getName();
        // TODO
        return null;
    }*/
}
