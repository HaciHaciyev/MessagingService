package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.articles.enumerations.CommentType;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;

public record Reference(CommentType commentType, @Nullable UUID parentCommentID) {

    public Reference {
        Objects.requireNonNull(commentType);
        if (commentType.equals(CommentType.CHILD)) {
            Objects.requireNonNull(parentCommentID);
        }
    }
}