package core.project.messaging.application.service;

import core.project.messaging.application.dto.articles.ArticleDTO;
import core.project.messaging.application.dto.articles.ArticleForm;
import core.project.messaging.application.dto.articles.ArticlePreview;
import core.project.messaging.application.dto.articles.ArticleText;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.repositories.OutboundArticleRepository;
import core.project.messaging.domain.articles.services.ArticlesService;
import core.project.messaging.domain.articles.values_objects.ArticlesQueryForm;
import core.project.messaging.domain.user.value_objects.Username;
import core.project.messaging.infrastructure.mappers.ArticleMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static core.project.messaging.application.util.JSONUtilities.responseException;

@ApplicationScoped
public class ArticlesApplicationService {

    private final ArticleMapper mapper;

    private final ArticlesService articlesService;

    private final OutboundArticleRepository articleRepository;

    ArticlesApplicationService(ArticleMapper mapper,
                               ArticlesService articlesService,
                               OutboundArticleRepository articleRepository) {
        this.mapper = mapper;
        this.articlesService = articlesService;
        this.articleRepository = articleRepository;
    }

    public List<ArticlePreview> pageOf(ArticlesQueryForm query) {
        return articleRepository.page(query).orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "Invalid query"));
    }

    public List<ArticlePreview> pageOf(int pageNumber, int pageSize, String username) {
        return articleRepository.page(pageNumber, pageSize, new Username(username))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not exists"));
    }

    public List<ArticleDTO> archive(int pageNumber, int pageSize, String username) {
        List<Article> articles = articleRepository.archive(pageNumber, pageSize, new Username(username))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not exists"));
        return articles.stream().map(mapper::toDto).toList();
    }

    public List<ArticleDTO> draft(int pageNumber, int pageSize, String username) {
        List<Article> articles = articleRepository.draft(pageNumber, pageSize, new Username(username))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not exists"));
        return articles.stream().map(mapper::toDto).toList();
    }

    public ArticleDTO save(ArticleForm articleForm, String username) {
        Article article = articlesService.save(articleForm, username);
        return mapper.toDto(article);
    }

    public ArticleDTO changeStatus(String articleID, ArticleStatus status, String username) {
        Article article = articlesService.changeStatus(articleID, status, username);
        return mapper.toDto(article);
    }

    public ArticleDTO updateArticle(String articleID, ArticleText articleText, String username) {
        Article article = articlesService.updateArticle(articleID, articleText, username);
        return mapper.toDto(article);
    }

    public ArticleDTO addArticleTag(String articleID, String tag, String username) {
        Article article = articlesService.addArticleTag(articleID, tag, username);
        return mapper.toDto(article);
    }

    public ArticleDTO removeArticleTag(String articleID, String tag, String username) {
        Article article = articlesService.removeArticleTag(articleID, tag, username);
        return mapper.toDto(article);
    }

    public ArticleDTO viewArticle(String id, String username) {
        Article article = articlesService.viewArticle(id, username);
        return mapper.toDto(article);
    }
}
