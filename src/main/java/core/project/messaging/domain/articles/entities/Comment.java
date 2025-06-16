package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.values_objects.CommentDates;
import core.project.messaging.domain.articles.values_objects.CommentIdentifiers;
import core.project.messaging.domain.articles.values_objects.CommentText;
import core.project.messaging.domain.articles.values_objects.Reference;
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Comment {
    private final CommentIdentifiers commentIdentifiers;
    private CommentText text;
    private int likes;
    private final Reference reference;
    private CommentDates events;

    private Comment(
            CommentIdentifiers commentIdentifiers,
            CommentText value,
            Reference reference,
            int likes,
            CommentDates events) {

        if (commentIdentifiers == null) throw new IllegalDomainArgumentException("Comment identifiers can't be null.");
        if (value == null) throw new IllegalDomainArgumentException("Comment text cannot be null.");
        if (reference == null) throw new IllegalDomainArgumentException("Reference cannot be null.");
        if (likes < 0) throw new IllegalDomainArgumentException("LikesCount can`t be negative.");

        this.commentIdentifiers = commentIdentifiers;
        this.text = value;
        this.reference = reference;
        this.events = events;
    }

    public static Comment of(CommentIdentifiers commentIdentifiers, CommentText value, Reference reference) {
        return new Comment(commentIdentifiers, value, reference, 0, new CommentDates(LocalDateTime.now(), LocalDateTime.now()));
    }

    public static Comment fromRepository(
            CommentIdentifiers commentIdentifiers,
            CommentText value,
            Reference reference,
            int likesCount,
            CommentDates events) {

        return new Comment(commentIdentifiers, value, reference, likesCount, events);
    }

    public UUID id() {
        return commentIdentifiers.commentID();
    }

    public UUID userId() {
        return commentIdentifiers.userID();
    }

    public UUID articleId() {
        return commentIdentifiers.articleID();
    }

    public CommentText text() {
        return text;
    }

    public int likes() {
        return likes;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        this.likes--;
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

    public CommentDates events() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(commentIdentifiers, comment.commentIdentifiers) &&
                Objects.equals(text, comment.text) &&
                Objects.equals(reference, comment.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentIdentifiers, text, reference);
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
                """, commentIdentifiers.commentID(),
                commentIdentifiers.userID(),
                commentIdentifiers.articleID(),
                text.value(),
                reference.commentType(),
                reference.parentCommentID(),
                reference.respondTo());
    }
}
