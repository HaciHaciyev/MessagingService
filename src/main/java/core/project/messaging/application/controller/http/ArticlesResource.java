package core.project.messaging.application.controller.http;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.dto.ArticleText;
import core.project.messaging.application.service.ArticlesApplicationService;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
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
        return Response.ok(articlesService.save(articleForm, jwt.getName())).build();
    }

    @PATCH
    @Path("/change-article-status")
    public Response changeArticleStatus(@QueryParam("articleID") String articleID,
                                        @QueryParam("status") ArticleStatus status) {
        return Response.accepted(articlesService.changeStatus(articleID, status, jwt.getName())).build();
    }

    @PUT
    @Path("/update-article")
    public Response updateArticle(@QueryParam("articleID") String articleID, ArticleText articleText) {
        return Response.accepted(articlesService.updateArticle(articleID, articleText, jwt.getName())).build();
    }

    @GET
    @Path("/viewArticle")
    public Response viewArticle(@QueryParam("id") String id) {
        return Response.ok(articlesService.viewArticle(id, jwt.getName())).build();
    }
}
