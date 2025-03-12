package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.articles.enumerations.CommentType;

import java.util.Objects;

public record CommentInfo(CommentIdentifiers commentIdentifiers, CommentType commentType) {

    public CommentInfo {
        Objects.requireNonNull(commentIdentifiers);
        Objects.requireNonNull(commentType);
    }
}
