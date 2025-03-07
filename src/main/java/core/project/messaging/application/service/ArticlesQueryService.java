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

            UserAccount userAccount = outboundUserRepository
                    .findByUsername(new Username(name))
                    .orElseThrow(() -> new WebApplicationException(Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Account is not exists.")
                            .build()));

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
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build());
        }
    }

    public Article findByID(String articleID, String username) {
        try {
            UserAccount user = outboundUserRepository
                    .findByUsername(new Username(username))
                    .orElseThrow(() -> new WebApplicationException(Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Account is not exists.")
                            .build()));

            Article article = outboundArticleRepository
                    .findByID(UUID.fromString(articleID))
                    .orElseThrow(() -> new WebApplicationException(Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Article is not exists.")
                            .build()));

            final boolean isNotPublished = !article.status().equals(ArticleStatus.PUBLISHED);
            final boolean isNotAuthor = !article.authorId().equals(user.getId());

            if (isNotPublished && isNotAuthor) {
                throw new WebApplicationException(Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Article not found")
                        .build());
            }

            return article;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build());
        }
    }
}