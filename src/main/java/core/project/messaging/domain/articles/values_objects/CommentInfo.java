package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.articles.enumerations.CommentType;

public record CommentInfo(CommentIdentifiers commentIdentifiers, CommentType commentType) {

    public CommentInfo {
        if (commentIdentifiers == null) throw new IllegalArgumentException("Comment identifiers must not be null");
        if (commentType == null) throw new IllegalArgumentException("Comment type must not be null");
    }
}
