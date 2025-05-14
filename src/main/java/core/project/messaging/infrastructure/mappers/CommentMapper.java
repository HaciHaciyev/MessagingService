package core.project.messaging.infrastructure.mappers;

import core.project.messaging.application.dto.articles.CommentDTO;
import core.project.messaging.domain.articles.entities.Comment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentMapper {
    public CommentDTO toDto(Comment comment) {
        return new CommentDTO(
                comment.id(),
                comment.userId(),
                comment.articleId(),
                comment.text().value(),
                comment.likes(),
                comment.reference().commentType(),
                comment.reference().parentCommentID(),
                comment.reference().respondTo()
        );
    }
}