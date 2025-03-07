package core.project.messaging.application.dto;

import core.project.messaging.domain.articles.enumerations.ArticleStatus;

import java.util.List;

public record ArticleForm(String header, String summary, String body, ArticleStatus status, List<String> tags) {}
