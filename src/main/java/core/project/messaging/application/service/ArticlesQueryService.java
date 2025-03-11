package core.project.messaging.application.service;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import core.project.messaging.domain.articles.values_objects.Body;
import core.project.messaging.domain.articles.values_objects.Header;
import core.project.messaging.domain.articles.values_objects.Summary;
import core.project.messaging.domain.user.entities.UserAccount;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.dal.repository.outbound.OutboundUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArticlesQueryService {

    private final OutboundUserRepository outboundUserRepository;

    private final InboundArticleRepository inboundArticleRepository;

    private final OutboundArticleRepository outboundArticleRepository;

    ArticlesQueryService(InboundArticleRepository inboundArticleRepository,
                         OutboundUserRepository outboundUserRepository,
                         OutboundArticleRepository outboundArticleRepository) {

        this.inboundArticleRepository = inboundArticleRepository;
        this.outboundUserRepository = outboundUserRepository;
        this.outboundArticleRepository = outboundArticleRepository;
    }

    public void save(ArticleForm articleForm, String name) {
        try {
            Objects.requireNonNull(articleForm);

            UserAccount userAccount = getUser(name);
            Set<ArticleTag> articleTags = articleForm
                    .tags()
                    .stream()
                    .map(ArticleTag::new)
                    .collect(Collectors.toSet());

            inboundArticleRepository.save(Article.of(
                    userAccount.getId(),
                    articleTags,
                    new Header(articleForm.header()),
                    new Summary(articleForm.summary()),
                    new Body(articleForm.body()),
                    articleForm.status()
            ));
        } catch (NullPointerException | IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Article findByID(String articleID, String username) {
        try {
            UserAccount user = getUser(username);
            Article article = outboundArticleRepository
                    .findByID(UUID.fromString(articleID))
                    .orElseThrow(() -> getWebApplicationException(Response.Status.BAD_REQUEST, "Article is not exists."));

            final boolean isNotPublished = !article.status().equals(ArticleStatus.PUBLISHED);
            final boolean isNotAuthor = !article.authorId().equals(user.getId());

            if (isNotPublished && isNotAuthor) {
                throw getWebApplicationException(Response.Status.NOT_FOUND, "Article not found");
            }

            return article;
        } catch (IllegalArgumentException e) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Article> pageOfArticles(int pageNumber, int pageSize, String username) {
        if (!Username.validate(username)) {
            throw getWebApplicationException(Response.Status.BAD_REQUEST, "Invalid username");
        }

        return outboundArticleRepository.page(pageNumber, pageSize).orElseThrow(() ->
                getWebApplicationException(Response.Status.BAD_REQUEST, "Can`t find an articles page"));
    }

    private static WebApplicationException getWebApplicationException(Response.Status badRequest, String o) {
        return new WebApplicationException(Response
                .status(badRequest)
                .entity(o)
                .build());
    }

    private UserAccount getUser(String username) {
        return outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> getWebApplicationException(Response.Status.BAD_REQUEST, "Account is not exists."));
    }
}