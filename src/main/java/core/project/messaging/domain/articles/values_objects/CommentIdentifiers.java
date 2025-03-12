package core.project.messaging.domain.articles.values_objects;

import java.util.Objects;
import java.util.UUID;

public record CommentIdentifiers(UUID commentID, UUID userID, UUID articleID) {

    public CommentIdentifiers {
        Objects.requireNonNull(articleID);
        Objects.requireNonNull(commentID);
        Objects.requireNonNull(userID);
    }
}
