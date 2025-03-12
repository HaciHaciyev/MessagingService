package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Comment;

public interface InboundCommentRepository {

    void save(Comment comment);
}
