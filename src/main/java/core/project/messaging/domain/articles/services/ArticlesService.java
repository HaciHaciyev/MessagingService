package core.project.messaging.domain.articles.services;

import core.project.messaging.application.dto.ArticleForm;
import core.project.messaging.application.dto.ArticleText;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.entities.View;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.repositories.InboundArticleRepository;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.values_objects.*;
import core.project.messaging.domain.commons.containers.Result;
import core.project.messaging.domain.user.entities.User;
import core.project.messaging.domain.user.repositories.OutboundUserRepository;
import core.project.messaging.domain.user.value_objects.Username;
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

    public static final int SEARCH_QUERY_MIN_SIZE = 3;

    public static final int SEARCH_QUERY_MAX_SIZE = 64;

    ArticlesService(OutboundUserRepository outboundUserRepository,
                    InboundArticleRepository inboundArticleRepository,
                    OutboundArticleRepository outboundArticleRepository) {
        this.outboundUserRepository = outboundUserRepository;
        this.inboundArticleRepository = inboundArticleRepository;
        this.outboundArticleRepository = outboundArticleRepository;
    }

    public Article save(ArticleForm articleForm, String username) {
        validateUsername(username);

        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account is not exists."));

        Set<ArticleTag> articleTags = articleForm
                .tags()
                .stream()
                .map(ArticleTag::new)
                .collect(Collectors.toSet());

        Article article = Article.of(
                user.id(),
                articleTags,
                new Header(articleForm.header()),
                new Summary(articleForm.summary()),
                new Body(articleForm.body()),
                articleForm.status()
        );

        inboundArticleRepository.save(article);
        return article;
    }

    public Result<Article, IllegalArgumentException> viewArticle(String articleID, String username) {
        validateUsername(username);

        Result<User, Throwable> user = outboundUserRepository.findByUsername(username);
        if (!user.success()) {
            return Result.failure(new IllegalArgumentException("User not found."));
        }

        Result<Article, Throwable> articleResult = outboundArticleRepository.article(UUID.fromString(articleID));
        if (!articleResult.success()) {
            return Result.failure(new IllegalArgumentException("Article is not exists."));
        }

        final boolean isNotPublished = !articleResult.value().status().equals(ArticleStatus.PUBLISHED);
        final boolean isNotAuthor = !articleResult.value().authorId().equals(user.value().id());

        if (isNotPublished && isNotAuthor) {
            return Result.failure(new IllegalArgumentException("Article not found"));
        }

        Article article = articleResult.value();
        if (!isNotPublished) {
            article.incrementViews();
            View view = new View(UUID.randomUUID(), article.id(), user.value().id(), LocalDateTime.now());
            inboundArticleRepository.updateViews(view);
        }

        return Result.success(article);
    }

    public void deleteView(String articleID, String username) {
        validateUsername(username);

        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        inboundArticleRepository.deleteView(UUID.fromString(articleID), user.id());
    }

    public void likeArticle(String articleID, String username) {
        validateUsername(username);

        Article article = outboundArticleRepository
                .article(UUID.fromString(articleID))
                .orElseThrow(() -> new IllegalArgumentException("Can`t find article"));

        article.incrementLikes();

        final boolean isNotPublished = !article.status().equals(ArticleStatus.PUBLISHED);
        if (isNotPublished) {
            throw new IllegalArgumentException("Article not found");
        }

        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!outboundArticleRepository.isViewExists(article.id(), user.id())) {
            throw new IllegalArgumentException("If a user has never read this article, he is not able to like it.");
        }

        Like like = new Like(article.id(), user.id(), LocalDateTime.now());
        inboundArticleRepository.updateLikes(like);
    }

    public void deleteLike(String articleID, String username) {
        validateUsername(username);

        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        inboundArticleRepository.deleteLike(UUID.fromString(articleID), user.id());
    }

    public Article changeStatus(String articleID, ArticleStatus status, String username) {
        validateUsername(username);
        if (status.equals(ArticleStatus.DRAFT)) {
            throw new IllegalArgumentException("You can`t draft article after it was published or archived.");
        }

        Article article = outboundArticleRepository
                .article(UUID.fromString(articleID))
                .orElseThrow(() -> new IllegalArgumentException("Can`t find article."));

        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Can`t find user."));

        final boolean isAuthor = user.id().equals(article.authorId());
        if (!isAuthor) {
            throw new IllegalArgumentException("Article status can be changed only by article author.");
        }

        if (article.status().equals(ArticleStatus.ARCHIVED)) {
            article.archive();
        } else {
            article.publish();
        }

        inboundArticleRepository.statusChange(article);
        return article;
    }

    public Article updateArticle(String articleID, ArticleText articleText, String username) {
        validateUsername(username);
        if (Objects.isNull(articleText.header()) && Objects.isNull(articleText.summary()) && Objects.isNull(articleText.body())) {
            throw new IllegalArgumentException("Seriously?...");
        }

        Article article = outboundArticleRepository
                .article(UUID.fromString(articleID))
                .orElseThrow(() -> new IllegalArgumentException("Can`t find article."));

        User user = outboundUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Can`t find user."));

        final boolean isAuthor = user.id().equals(article.authorId());
        if (!isAuthor) {
            throw new IllegalArgumentException("Article status can be changed only by article author.");
        }

        if (Objects.nonNull(articleText.header())) {
            article.changeHeader(new Header(articleText.header()));
            inboundArticleRepository.updateHeader(article);
        }
        if (Objects.nonNull(articleText.summary())) {
            article.changeSummary(new Summary(articleText.summary()));
            inboundArticleRepository.updateSummary(article);
        }
        if (Objects.nonNull(articleText.body())) {
            article.changeBody(new Body(articleText.body()));
            inboundArticleRepository.updateBody(article);
        }

        return article;
    }

    static void validateUsername(String username) {
        if (!Username.validate(username)) {
            throw new IllegalArgumentException("Invalid username");
        }
    }
}
