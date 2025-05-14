package core.project.messaging.application.dto.articles;

import core.project.messaging.domain.articles.enumerations.CommentType;

import java.util.UUID;

public record CommentDTO(
    UUID commentID,
    UUID userID,
    UUID articleID,
    String text,
    int likes,
    CommentType commentType,
    UUID parentCommentID,
    UUID respondTo
) {}
