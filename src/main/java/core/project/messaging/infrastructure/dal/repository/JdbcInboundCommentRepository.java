package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jdbclight.jdbc.JDBC;
import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.repositories.InboundCommentRepository;
import core.project.messaging.domain.articles.values_objects.CommentLike;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static com.hadzhy.jdbclight.sql.SQLBuilder.*;

@Transactional
@ApplicationScoped
public class JdbcInboundCommentRepository implements InboundCommentRepository {

    private final JDBC jdbc;

    static final String SAVE_COMMENT = insert()
            .into("Comments")
            .column("id")
            .column("article_id")
            .column("user_id")
            .column("text")
            .column("comment_type")
            .column("parent_comment_id")
            .column("respond_to_comment")
            .column("creation_date")
            .column("last_updated")
            .values()
            .build()
            .sql();

    static final String DELETE_COMMENT = delete()
            .from("Comments")
            .where("id = ?")
            .and("user_id = ?")
            .build()
            .sql();

    static final String UPDATE_COMMENT = update("Comments")
            .set("text = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String COMMENT_LIKE = insert()
            .into("CommentLikes")
            .columns("comment_id", "user_id", "creation_date")
            .values()
            .build()
            .sql();

    static final String DELETE_COMMENT_LIKE = delete()
            .from("Likes")
            .where("comment_id = ?")
            .and("user_id = ?")
            .build()
            .sql();

    JdbcInboundCommentRepository() {
        this.jdbc = JDBC.instance();
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
                        comment.events().lastUpdatedDate())
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
    public void like(CommentLike commentLike) {
        jdbc.write(COMMENT_LIKE, commentLike.commentId().toString(), commentLike.userId().toString(), commentLike.likedAt())
                .ifFailure(throwable -> Log.errorf("Error updating comment likes: %s", throwable.getMessage()));
    }

    @Override
    public void deleteLike(UUID commentID, UUID userID) {
        jdbc.write(DELETE_COMMENT_LIKE, commentID.toString(), userID.toString())
                .ifFailure(throwable -> Log.errorf("Error deleting comment like: %s", throwable.getMessage()));
    }
}
