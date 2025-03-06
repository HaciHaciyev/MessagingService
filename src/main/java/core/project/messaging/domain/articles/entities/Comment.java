package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.enumerations.ReferenceOn;
import core.project.messaging.domain.articles.values_objects.CommentText;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Comment {
    private final UUID id;
    private final UUID userId;
    private final UUID articleId;
    private CommentText text;
    private final ReferenceOn referenceOn;
    private final UUID referencedCommentId;

    public Comment(UUID id, UUID userId, UUID articleId, CommentText value, ReferenceOn referenceOn, UUID referencedCommentId) {
        Objects.requireNonNull(id, "ID cannot be null.");
        Objects.requireNonNull(userId, "UserID cannot be null.");
        Objects.requireNonNull(articleId, "ArticleID cannot be null.");
        Objects.requireNonNull(value, "Value cannot be null.");
        Objects.requireNonNull(referenceOn, "Reference on cannot be null. Comment must be linked either to an another comment or article itself.");
        if (referenceOn.equals(ReferenceOn.COMMENT)) {
            Objects.requireNonNull(referencedCommentId, "A link to another comment cannot be null if the comment links to another comment.");
        }

        this.id = id;
        this.userId = userId;
        this.articleId = articleId;
        this.text = value;
        this.referenceOn = referenceOn;
        this.referencedCommentId = referencedCommentId;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public UUID articleId() {
        return articleId;
    }

    public CommentText text() {
        return text;
    }

    public void changeText(CommentText commentValue) {
        Objects.requireNonNull(commentValue, "Comment text cannot be null.");
        this.text = commentValue;
    }

    public void delete() {
        this.text = new CommentText(CommentText.DELETED_COMMENT);
    }

    public ReferenceOn referenceOn() {
        return referenceOn;
    }

    public Optional<UUID> referencedCommentId() {
        return Optional.ofNullable(referencedCommentId);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Comment comment)) return false;

        return id.equals(comment.id) &&
                Objects.equals(text, comment.text) &&
                referenceOn == comment.referenceOn &&
                Objects.equals(referencedCommentId, comment.referencedCommentId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Objects.hashCode(text);
        result = 31 * result + referenceOn.hashCode();
        result = 31 * result + Objects.hashCode(referencedCommentId);
        return result;
    }

    @Override
    public String toString() {
        return String.format("""
                Comment {
                    ID: %s,
                    Author ID: %s,
                    Article ID: %s,
                    Text: %s,
                    Referenced on: %s,
                    Referenced comment ID: %s
                }
                """, id, userId, articleId, text.value(), referenceOn, referencedCommentId);
    }
}
