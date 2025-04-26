package core.project.messaging.domain.articles.values_objects;

import java.util.UUID;

public record CommentIdentifiers(UUID commentID, UUID userID, UUID articleID) {

    public CommentIdentifiers {
        if (commentID == null) throw new IllegalArgumentException("Comment ID must not be null");
        if (userID == null) throw new IllegalArgumentException("User ID must not be null");
        if (articleID == null) throw new IllegalArgumentException("Article ID must not be null");
    }
}
