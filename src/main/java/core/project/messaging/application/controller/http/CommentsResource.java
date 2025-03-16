package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.CommentForm;
import core.project.messaging.application.service.CommentsQueryService;
import core.project.messaging.domain.articles.services.CommentsService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static core.project.messaging.application.controller.http.ArticlesResource.nonNull;

@Authenticated
@Path("/articles/comments")
public class CommentsResource {

    private final JsonWebToken jwt;

    private final CommentsService commentsService;

    private final CommentsQueryService queryService;

    CommentsResource(JsonWebToken jwt, CommentsService commentsService, CommentsQueryService queryService) {
        this.jwt = jwt;
        this.commentsService = commentsService;
        this.queryService = queryService;
    }

    @POST
    @Path("/create")
    public Response create(CommentForm commentForm) {
        nonNull(commentForm);
        commentsService.create(commentForm, jwt.getName());
        return Response.accepted().build();
    }

    @PATCH
    @Path("/edit")
    public Response edit(@QueryParam("commentID") String commentID, String text) {
        nonNull(commentID, text);
        return Response.accepted(commentsService.edit(commentID, text, jwt.getName())).build();
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("commentID") String commentID) {
        nonNull(commentID);
        commentsService.delete(commentID, jwt.getName());
        return Response.accepted().build();
    }

    @GET
    @Path("/page")
    public Response commentsPageOf(@QueryParam("articleID") String articleID,
                                   @QueryParam("pageNumber") int pageNumber,
                                   @QueryParam("pageSize") int pageSize) {
        nonNull(articleID);
        return Response.ok(queryService.pageOf(articleID, pageNumber, pageSize)).build();
    }

    @GET
    @Path("/page/child-comments")
    public Response commentPageOf(@QueryParam("articleID") String articleID,
                                  @QueryParam("parentCommentID") String parentCommentID,
                                  @QueryParam("pageNumber") int pageNumber,
                                  @QueryParam("pageSize") int pageSize) {
        nonNull(articleID, parentCommentID);
        return Response.ok(queryService.pageOf(articleID, parentCommentID, pageNumber, pageSize)).build();
    }
}
