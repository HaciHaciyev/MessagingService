package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

public record ArticleTag(String value) {

    public static final int MAX_SIZE = 24;

    public ArticleTag {
        if (value == null) {
            throw new IllegalDomainArgumentException("ArticleTag cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalDomainArgumentException("Tag cannot be blank.");
        }
        if (value.length() > MAX_SIZE) {
            throw new IllegalDomainArgumentException("Tag must be less than 56 characters.");
        }
    }

    public static void validate(String value) {
        if (value == null) {
            throw new IllegalDomainArgumentException("ArticleTag cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalDomainArgumentException("Tag cannot be blank.");
        }
        if (value.length() > MAX_SIZE) {
            throw new IllegalDomainArgumentException("Tag must be less than 56 characters.");
        }
    }
}