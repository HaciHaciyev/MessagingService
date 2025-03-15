package core.project.messaging.application.dto;

import core.project.messaging.domain.articles.enumerations.ArticleStatus;

import java.util.List;

public record ArticleForm(String header, String summary, String body, ArticleStatus status, List<String> tags) {

    public ArticleForm {
        if (header == null || summary == null || body == null || status == null || tags == null) {
            throw new IllegalArgumentException("No one of required parameters can be null");
        }
    }
}
