package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.articles.ArticleForm;
import core.project.messaging.application.dto.articles.ArticleText;
import core.project.messaging.application.service.ArticlesApplicationService;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.values_objects.ArticlesQueryForm;
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
        nonNull(articleForm);
        return Response.ok(articlesService.save(articleForm, jwt.getName())).build();
    }

    @PATCH
    @Path("/change-article-status")
    public Response changeArticleStatus(@QueryParam("articleID") String articleID,
                                        @QueryParam("status") ArticleStatus status) {
        nonNull(articleID, status);
        return Response.accepted(articlesService.changeStatus(articleID, status, jwt.getName())).build();
    }

    @PUT
    @Path("/update-article")
    public Response updateArticle(@QueryParam("articleID") String articleID, ArticleText articleText) {
        nonNull(articleID, articleText);
        return Response.accepted(articlesService.updateArticle(articleID, articleText, jwt.getName())).build();
    }

    @PATCH
    @Path("/add-article")
    public Response addArticle(@QueryParam("articleID") String articleID, @QueryParam("tag") String tag) {
        nonNull(articleID, tag);
        return Response.accepted(articlesService.addArticleTag(articleID, tag, jwt.getName())).build();
    }

    @PATCH
    @Path("/remove-tag")
    public Response removeTag(@QueryParam("articleID") String articleID, @QueryParam("tag") String tag) {
        nonNull(articleID, tag);
        return Response.accepted(articlesService.removeArticleTag(articleID, tag, jwt.getName())).build();
    }

    @GET
    @Path("/viewArticle")
    public Response viewArticle(@QueryParam("id") String id) {
        nonNull(id);
        return Response.ok(articlesService.viewArticle(id, jwt.getName())).build();
    }

    @GET
    @Path("/page")
    public Response pageOf(ArticlesQueryForm query) {
        nonNull(query);
        return Response.ok(articlesService.pageOf(query)).build();
    }

    @GET
    @Path("home-page")
    public Response pageOf(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(articlesService.pageOf(pageNumber, pageSize, jwt.getName())).build();
    }

    @GET
    @Path("/archive")
    public Response archive(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(articlesService.archive(pageNumber, pageSize, jwt.getName())).build();
    }

    @GET
    @Path("/draft")
    public Response draft(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(articlesService.draft(pageNumber, pageSize, jwt.getName())).build();
    }

    static void nonNull(Object... values) {
        for (Object value : values) {
            if (value == null) throw new IllegalArgumentException("The accepted value turned out to be null.");
        }
    }
}
