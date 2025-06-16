package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.time.LocalDateTime;

public record ArticleDates(LocalDateTime creationDate, LocalDateTime lastUpdateDate) {

    public ArticleDates {
        if (creationDate == null)
            throw new IllegalDomainArgumentException("Creation date can`t be null.");
        if (lastUpdateDate == null)
            throw new IllegalDomainArgumentException("Last update date can`t be null.");
    }

    public static ArticleDates defaultEvents() {
        return new ArticleDates(LocalDateTime.now(), LocalDateTime.now());
    }
}
