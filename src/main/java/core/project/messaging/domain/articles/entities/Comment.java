package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.values_objects.CommentText;
import core.project.messaging.domain.articles.values_objects.Reference;

import java.util.Objects;
import java.util.UUID;

public class Comment {
    private final UUID id;
    private final UUID userId;
    private final UUID articleId;
    private CommentText text;
    private final Reference reference;

    public Comment(UUID id, UUID userId, UUID articleId, CommentText value, Reference reference) {
        Objects.requireNonNull(id, "ID cannot be null.");
        Objects.requireNonNull(userId, "UserID cannot be null.");
        Objects.requireNonNull(articleId, "ArticleID cannot be null.");
        Objects.requireNonNull(value, "Comment text cannot be null.");
        Objects.requireNonNull(reference, "Reference cannot be null.");

        this.id = id;
        this.userId = userId;
        this.articleId = articleId;
        this.text = value;
        this.reference = reference;
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

    public Reference reference() {
        return reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
                Objects.equals(userId, comment.userId) &&
                Objects.equals(articleId, comment.articleId) &&
                Objects.equals(text, comment.text) &&
                Objects.equals(reference, comment.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, articleId, text, reference);
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
                    Responded to comment: %s
                }
                """, id,
                userId,
                articleId,
                text.value(),
                reference.commentType(),
                reference.parentCommentID(),
                reference.respondTo());
    }
}
