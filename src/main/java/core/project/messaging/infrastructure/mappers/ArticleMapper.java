package core.project.messaging.infrastructure.mappers;

import core.project.messaging.application.dto.articles.ArticleDTO;
import core.project.messaging.domain.articles.entities.Article;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Collectors;

@ApplicationScoped
public class ArticleMapper {
    public ArticleDTO toDto(Article article) {
        return new ArticleDTO(
            article.id(),
            article.authorId(),
            article.tags().stream()
                .map(ArticleTag::value)
                .collect(Collectors.toSet()),
            article.views(),
            article.likes(),
            article.header().value(),
            article.summary().value(),
            article.body().value(),
            article.status().name()
        );
    }
}
