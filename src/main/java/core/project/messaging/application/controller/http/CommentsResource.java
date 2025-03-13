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

    @PATCH
    @Path("/edit")
    public Response edit(@QueryParam("commentID") String commentID, String text) {
        return Response.accepted(articlesApplicationService.editComment(commentID, text, jwt.getName())).build();
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("commentID") String commentID) {
        articlesApplicationService.deleteComment(commentID, jwt.getName());
        return Response.accepted().build();
    }

    @GET
    @Path("/page")
    public Response commentsPageOf(@QueryParam("articleID") String articleID,
                                  @QueryParam("pageNumber") int pageNumber,
                                  @QueryParam("pageSize") int pageSize) {

        return Response.ok(articlesApplicationService.commentsPageOf(articleID, pageNumber, pageSize)).build();
    }

    @GET
    @Path("/page")
    public Response commentPageOf(@QueryParam("articleID") String articleID,
                                  @QueryParam("parentCommentID") String parentCommentID,
                                  @QueryParam("pageNumber") int pageNumber,
                                  @QueryParam("pageSize") int pageSize) {

        return Response.ok(articlesApplicationService.commentsPageOf(articleID, parentCommentID, pageNumber, pageSize)).build();
    }
}
