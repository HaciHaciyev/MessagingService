package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.articles.enumerations.CommentType;
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

public record CommentInfo(CommentIdentifiers commentIdentifiers, CommentType commentType) {

    public CommentInfo {
        if (commentIdentifiers == null) throw new IllegalDomainArgumentException("Comment identifiers must not be null");
        if (commentType == null) throw new IllegalDomainArgumentException("Comment type must not be null");
    }
}
