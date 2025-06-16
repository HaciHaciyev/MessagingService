package core.project.messaging.domain.articles.values_objects;

import core.project.messaging.domain.articles.enumerations.CommentType;
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;

public record Reference(CommentType commentType,
                        @Nullable UUID parentCommentID,
                        @Nullable UUID respondTo) {

    public Reference {
        if (commentType == null) throw new IllegalDomainArgumentException("Comment type must not be null");
        if (commentType == CommentType.CHILD && parentCommentID == null)
            throw new IllegalDomainArgumentException("Parent comment id can`t be null, if it`s a child type comment.");


        final boolean parentHasUnexpectedReference = commentType.equals(CommentType.PARENT) &&
                (Objects.nonNull(parentCommentID) || Objects.nonNull(respondTo));

        if (parentHasUnexpectedReference)
            throw new IllegalDomainArgumentException("Parent type comment can`t me referenced to another comment");
    }
}