package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.events.CommentEditedEvent;
import core.project.messaging.domain.articles.events.CommentEvents;
import core.project.messaging.domain.articles.values_objects.CommentIdentifiers;
import core.project.messaging.domain.articles.values_objects.CommentText;
import core.project.messaging.domain.articles.values_objects.Reference;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Comment {
    private final CommentIdentifiers commentIdentifiers;
    private CommentText text;
    private int likes;
    private final Reference reference;
    private CommentEvents events;

    private Comment(CommentIdentifiers commentIdentifiers, CommentText value, Reference reference, int likes, CommentEvents events) {
        Objects.requireNonNull(commentIdentifiers, "Comment identifiers can`t be null.");
        Objects.requireNonNull(value, "Comment text cannot be null.");
        Objects.requireNonNull(reference, "Reference cannot be null.");
        if (likes < 0) {
            throw new IllegalArgumentException("LikesCount can`t be negative.");
        }

        this.commentIdentifiers = commentIdentifiers;
        this.text = value;
        this.reference = reference;
        this.events = events;
    }

    public static Comment of(CommentIdentifiers commentIdentifiers, CommentText value, Reference reference) {
        return new Comment(commentIdentifiers, value, reference, 0, new CommentEvents(LocalDateTime.now(), LocalDateTime.now()));
    }

    public static Comment fromRepository(CommentIdentifiers commentIdentifiers,
                                         CommentText value,
                                         Reference reference,
                                         int likesCount,
                                         CommentEvents events) {

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

    public CommentEditedEvent changeText(CommentText commentValue) {
        Objects.requireNonNull(commentValue, "Comment text cannot be null.");
        this.text = commentValue;
        return updateEvent();
    }

    public void delete() {
        this.text = new CommentText(CommentText.DELETED_COMMENT);
    }

    public Reference reference() {
        return reference;
    }

    public CommentEvents events() {
        return events;
    }

    private CommentEditedEvent updateEvent() {
        this.events = new CommentEvents(events.creationDate(), LocalDateTime.now());
        return new CommentEditedEvent(id());
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
