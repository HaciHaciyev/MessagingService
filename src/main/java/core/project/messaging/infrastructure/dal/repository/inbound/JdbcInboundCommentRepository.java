package core.project.messaging.infrastructure.dal.repository.inbound;

import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.events.CommentEditedEvent;
import core.project.messaging.domain.articles.repositories.InboundCommentRepository;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.*;

@Transactional
@ApplicationScoped
public class JdbcInboundCommentRepository implements InboundCommentRepository {

    private final JDBC jdbc;

    static final String SAVE_COMMENT = insert()
            .into("Comments")
            .columns("id",
                    "article_id",
                    "user_id",
                    "text",
                    "comment_type",
                    "parent_comment_id",
                    "respond_to_comment",
                    "creation_date",
                    "last_updated"
            )
            .values(9)
            .build();

    static final String DELETE_COMMENT = delete()
            .from("Comments")
            .where("id = ?")
            .and("user_id = ?")
            .build();

    static final String UPDATE_COMMENT = update("Comments")
            .set("text = ?")
            .where("id = ?")
            .build();

    static final String UPDATE_DATE = update("Comments")
            .set("last_updated = ?")
            .where("id = ?")
            .build();

    JdbcInboundCommentRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Comment comment) {
        jdbc.write(SAVE_COMMENT,
                comment.id().toString(),
                comment.articleId().toString(),
                comment.userId().toString(),
                comment.text().value(),
                comment.reference().commentType().toString(),
                comment.reference().parentCommentID().toString(),
                comment.reference().respondTo().toString(),
                comment.events().creationDate(),
                comment.events().lastUpdatedDate()
        )
                .ifFailure(throwable -> Log.errorf("Error saving comment: %s", throwable.getMessage()));
    }

    @Override
    public void deleteComment(UUID commentID, UUID authorID) {
        jdbc.write(DELETE_COMMENT, commentID.toString(), authorID.toString())
                .ifFailure(throwable -> Log.errorf("Error deleting comment: %s", throwable.getMessage()));
    }

    @Override
    public void updateCommentText(Comment comment) {
        jdbc.write(UPDATE_COMMENT, comment.text().value(), comment.id().toString())
                .ifFailure(throwable -> Log.errorf("Error updating comment: %s", throwable.getMessage()));
    }

    @Override
    public void updateEvent(CommentEditedEvent commentEvent) {
        jdbc.write(UPDATE_DATE, commentEvent.data(), commentEvent.commentID().toString())
                .ifFailure(throwable -> Log.errorf("Error updating comment date: %s", throwable.getMessage()));
    }
}
