package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.dto.ArticleText;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.services.ArticlesService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Authenticated
@Path("/articles")
public class ArticlesResource {

    private final JsonWebToken jwt;

    private final ArticlesService articlesService;

    ArticlesResource(JsonWebToken jwt, ArticlesService articlesService) {
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

    @GET
    @Path("/viewArticle")
    public Response viewArticle(@QueryParam("id") String id) {
        nonNull(id);
        return Response.ok(articlesService.viewArticle(id, jwt.getName())).build();
    }

    static void nonNull(Object... values) {
        for (Object value : values) {
            if (value == null) throw new IllegalArgumentException("The accepted value turned out to be null.");
        }
    }
}
