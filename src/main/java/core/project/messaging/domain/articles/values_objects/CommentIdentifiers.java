package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.UUID;

public record CommentIdentifiers(UUID commentID, UUID userID, UUID articleID) {

    public CommentIdentifiers {
        if (commentID == null) throw new IllegalDomainArgumentException("Comment ID must not be null");
        if (userID == null) throw new IllegalDomainArgumentException("User ID must not be null");
        if (articleID == null) throw new IllegalDomainArgumentException("Article ID must not be null");
    }
}
