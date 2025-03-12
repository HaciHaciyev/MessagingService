package core.project.messaging.domain.articles.repositories;

import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.values_objects.CommentInfo;
import core.project.messaging.infrastructure.utilities.containers.Result;

import java.util.List;
import java.util.UUID;

public interface OutboundCommentRepository {

    boolean isCommentExists(UUID commentID);

    Result<CommentInfo, Throwable> commentInfo(UUID comment);

    Result<Comment, Throwable> comment(UUID comment);

    Result<List<Comment>, Throwable> page(UUID articleID, int limit, int offSet);
}
