package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.events.CommentEditedEvent;
import core.project.messaging.domain.articles.values_objects.CommentLike;

import java.util.UUID;

public interface InboundCommentRepository {

    void save(Comment comment);

    void deleteComment(UUID commentID, UUID authorID);

    void updateCommentText(Comment comment);

    void updateEvent(CommentEditedEvent commentEvent);

    void like(CommentLike commentLike);

    void deleteLike(UUID commentID, UUID userID);
}
