package core.project.messaging.infrastructure.dal.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.Order;
import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.enumerations.CommentType;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import core.project.messaging.domain.articles.values_objects.*;
import core.project.messaging.domain.commons.containers.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static core.project.messaging.infrastructure.dal.repository.JdbcOutboundArticleRepository.buildLimit;
import static core.project.messaging.infrastructure.dal.repository.JdbcOutboundArticleRepository.buildOffSet;

@ApplicationScoped
public class JdbcOutboundCommentRepository implements OutboundCommentRepository {

    private final JetQuerious jet;

    static final String IS_COMMENT_EXISTS = select()
            .column("id")
            .from("Comments")
            .where("id = ?")
            .build()
            .sql();

    static final String COMMENT_INFO = select()
            .column("id")
            .column("article_id")
            .column("user_id")
            .column("comment_type")
            .from("Comments")
            .where("id = ?")
            .build()
            .sql();

    static final String COMMENT = select()
            .column("c.id").as("id")
            .column("c.article_id").as("article_id")
            .column("c.user_id").as("user_id")
            .column("c.comment_type").as("comment_type")
            .column("c.parent_comment_id").as("parent_comment_id")
            .column("c.respond_to_comment").as("respond_to_comment")
            .column("c.creation_date").as("creation_date")
            .column("c.last_updated").as("last_updated")
            .count("cl.comment_id").as("likes_count")
            .from("Comments c")
            .join("CommentLikes cl", "cl.comment_id = c.id")
            .where("c.id = ?")
            .build()
            .sql();

    static final String COMMENTS = select()
            .column("c.id").as("id")
            .column("c.article_id").as("article_id")
            .column("c.user_id").as("user_id")
            .column("c.comment_type").as("comment_type")
            .column("c.parent_comment_id").as("parent_comment_id")
            .column("c.respond_to_comment").as("respond_to_comment")
            .column("c.creation_date").as("creation_date")
            .column("c.last_updated").as("last_updated")
            .count("cl.comment_id").as("likes_count")
            .from("Comments c")
            .join("CommentLikes cl", "cl.comment_id = c.id")
            .where("c.article_id = ?")
            .orderBy("likes_count", Order.DESC)
            .limitAndOffset()
            .sql();

    static final String CHILD_COMMENTS = select()
            .column("c.id").as("id")
            .column("c.article_id").as("article_id")
            .column("c.user_id").as("user_id")
            .column("c.comment_type").as("comment_type")
            .column("c.parent_comment_id").as("parent_comment_id")
            .column("c.respond_to_comment").as("respond_to_comment")
            .column("c.creation_date").as("creation_date")
            .column("c.last_updated").as("last_updated")
            .count("cl.comment_id").as("likes_count")
            .from("Comments c")
            .join("CommentLikes cl", "cl.comment_id = c.id")
            .where("c.article_id = ?")
            .and("c.parent_comment_id = ?")
            .orderBy("likes_count", Order.DESC)
            .limitAndOffset()
            .sql();

    JdbcOutboundCommentRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public boolean isCommentExists(UUID commentID) {
        return jet.readObjectOf(IS_COMMENT_EXISTS, Integer.class, commentID.toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking comment exists.");
                    return false;
                });
    }

    @Override
    public Result<CommentInfo, Throwable> commentInfo(UUID commentID) {
        var result = jet.read(COMMENT_INFO, this::commentInfoMapper, commentID.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<Comment, Throwable> comment(UUID commentID) {
        var result = jet.read(COMMENT, this::commentMapper, commentID.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<Comment>, Throwable> page(UUID articleID, int pageNumber, int pageSize) {
        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);
        var result = jet.readListOf(COMMENTS, this::commentMapper, articleID.toString(), limit, offSet);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<Comment>, Throwable> page(UUID articleID, UUID parentCommentID, int pageNumber, int pageSize) {
        int limit = buildLimit(pageSize);
        int offSet = buildOffSet(limit, pageNumber);
        var result = jet.readListOf(CHILD_COMMENTS, this::commentMapper, articleID.toString(), parentCommentID.toString(), limit, offSet);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private CommentInfo commentInfoMapper(ResultSet rs) throws SQLException {
        CommentIdentifiers commentIdentifiers = new CommentIdentifiers(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                UUID.fromString(rs.getString("article_id"))
        );

        return new CommentInfo(commentIdentifiers, CommentType.valueOf(rs.getString("comment_type")));
    }

    private Comment commentMapper(ResultSet rs) throws SQLException {
        CommentIdentifiers commentIdentifiers = new CommentIdentifiers(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                UUID.fromString(rs.getString("article_id"))
        );

        Reference reference = new Reference(
                CommentType.valueOf(rs.getString("comment_type")),
                UUID.fromString(rs.getString("parent_comment_id")),
                UUID.fromString(rs.getString("respond_to_comment"))
        );

        CommentDates commentDates = new CommentDates(
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("last_updated", Timestamp.class).toLocalDateTime()
        );

        return Comment.fromRepository(
                commentIdentifiers,
                new CommentText(rs.getString("text")),
                reference,
                rs.getInt("likes_count"),
                commentDates
        );
    }
}
