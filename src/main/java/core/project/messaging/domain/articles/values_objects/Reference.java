package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.articles.enumerations.CommentType;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;

public record Reference(CommentType commentType,
                        @Nullable UUID parentCommentID,
                        @Nullable UUID respondTo) {

    public Reference {
        if (commentType == null) {
            throw new IllegalArgumentException("Comment type must not be null");
        }
        if (commentType.equals(CommentType.CHILD)) {
            Objects.requireNonNull(parentCommentID);
        }

        final boolean parentHasUnexpectedReference = commentType.equals(CommentType.PARENT) &&
                (Objects.nonNull(parentCommentID) || Objects.nonNull(respondTo));

        if (parentHasUnexpectedReference) {
            throw new IllegalArgumentException("Parent type comment can`t me referenced to another comment");
        }
    }
}