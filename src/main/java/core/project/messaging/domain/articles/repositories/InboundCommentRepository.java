package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Comment;

import java.util.UUID;

public interface InboundCommentRepository {

    void save(Comment comment);

    void deleteComment(UUID commentID, UUID authorID);
}
