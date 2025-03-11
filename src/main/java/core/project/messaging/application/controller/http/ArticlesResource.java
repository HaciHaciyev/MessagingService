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

    /*@PUT
    @Path("/update")
    public Response put(ArticleForm articleForm) {
        String username = jwt.getName();
        // TODO
        return null;
    }

    @POST
    @Path("/add-tag")
    public Response addTag(@QueryParam("articleID") String articleID,
                           @QueryParam("tag") String tag) {
        String username = jwt.getName();
        // TODO
        return null;
    }

    @DELETE
    @Path("/delete-tag")
    public Response deleteTag(@QueryParam("articleID") String articleID,
                              @QueryParam("tag") String tag) {
        String username = jwt.getName();
        // TODO
        return null;
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("id") String id) {
        String username = jwt.getName();
        // TODO
        return null;
    }
*/
    @GET
    @Path("/findByID")
    public Response findByID(@QueryParam("id") String id) {
        return Response.ok(articlesService.findByID(id, jwt.getName())).build();
    }

/*    @GET
    @Path("/page")
    public Response pageOfArticles(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        // TODO later
        return null;
    }

    @GET
    @Path("/page")
    public Response pageOfArticles(ArticlesQueryForm query,
                                   @QueryParam("pageNumber") int pageNumber,
                                   @QueryParam("pageSize") int pageSize) {
        // TODO later
        return null;
    }

    @GET
    @Path("/page/history")
    public Response historyOfArticles(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        // TODO later
        return null;
    }

    @GET
    @Path("/page/history")
    public Response historyOfArticles(ArticlesQueryForm query,
                                      @QueryParam("pageNumber") int pageNumber,
                                      @QueryParam("pageSize") int pageSize) {
        // TODO later
        return null;
    }

    @GET
    @Path("/drafted")
    public Response draftedPages() {
        String username = jwt.getName();
        // TODO
        return null;
    }

    @GET
    @Path("/archived")
    public Response archivedPages() {
        String username = jwt.getName();
        // TODO
        return null;
    }*/
}
