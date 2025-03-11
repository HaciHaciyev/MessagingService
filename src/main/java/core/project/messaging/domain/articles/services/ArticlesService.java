package core.project.messaging.domain.articles.services;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
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
import core.project.messaging.infrastructure.utilities.containers.Result;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArticlesService {

    private final OutboundUserRepository outboundUserRepository;

    private final InboundArticleRepository inboundArticleRepository;

    private final OutboundArticleRepository outboundArticleRepository;

    ArticlesService(OutboundUserRepository outboundUserRepository,
                    InboundArticleRepository inboundArticleRepository,
                    OutboundArticleRepository outboundArticleRepository) {
        this.outboundUserRepository = outboundUserRepository;
        this.inboundArticleRepository = inboundArticleRepository;
        this.outboundArticleRepository = outboundArticleRepository;
    }

    public void save(ArticleForm articleForm, String username) {
        Objects.requireNonNull(articleForm);

        UserAccount userAccount = outboundUserRepository
                .findByUsername(new Username(username))
                .orElseThrow(() -> new IllegalArgumentException("Account is not exists."));

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
    }

    public Result<Article, IllegalArgumentException> viewArticle(String articleID, String username) {
        Result<UserAccount, Throwable> user = outboundUserRepository.findByUsername(new Username(username));
        if (!user.success()) {
            return Result.failure(new IllegalArgumentException("User not found."));
        }

        Result<Article, Throwable> articleResult = outboundArticleRepository.article(UUID.fromString(articleID));
        if (!articleResult.success()) {
            return Result.failure(new IllegalArgumentException("Article is not exists."));
        }

        final boolean isNotPublished = !articleResult.value().status().equals(ArticleStatus.PUBLISHED);
        final boolean isNotAuthor = !articleResult.value().authorId().equals(user.value().getId());

        if (isNotPublished && isNotAuthor) {
            return Result.failure(new IllegalArgumentException("Article not found"));
        }

        Article article = articleResult.value();
        article.incrementViews();

        View view = new View(UUID.randomUUID(), article.id(), user.value().getId(), LocalDateTime.now());

        inboundArticleRepository.updateViews(view);
        return Result.success(article);
    }

    public void deleteView(String articleID, String username) {
        inboundArticleRepository.deleteView(UUID.fromString(articleID), new Username(username));
    }
}
