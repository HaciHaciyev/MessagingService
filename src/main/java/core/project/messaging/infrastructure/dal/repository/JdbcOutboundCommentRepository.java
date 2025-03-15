package core.project.messaging.infrastructure.dal.repository;

import core.project.messaging.domain.articles.entities.Comment;
import core.project.messaging.domain.articles.enumerations.CommentType;
import core.project.messaging.domain.articles.repositories.OutboundCommentRepository;
import core.project.messaging.domain.articles.values_objects.CommentIdentifiers;
import core.project.messaging.domain.articles.values_objects.CommentInfo;
import core.project.messaging.domain.articles.values_objects.CommentText;
import core.project.messaging.domain.articles.values_objects.Reference;
import core.project.messaging.infrastructure.dal.util.jdbc.JDBC;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static core.project.messaging.infrastructure.dal.util.sql.SQLBuilder.select;

@Transactional
@ApplicationScoped
public class JdbcOutboundCommentRepository implements OutboundCommentRepository {

    private final JDBC jdbc;

    static final String IS_COMMENT_EXISTS = select()
            .column("id")
            .from("Comments")
            .where("id = ?")
            .build();

    static final String COMMENT_INFO = select()
            .column("id")
            .column("article_id")
            .column("user_id")
            .column("comment_type")
            .from("Comments")
            .where("id = ?")
            .build();

    static final String COMMENT = select()
            .all()
            .from("Comments")
            .where("id = ?")
            .build();

    static final String COMMENTS = select()
            .all()
            .from("Comments")
            .where("article_id = ?")
            .limitAndOffset();

    static final String CHILD_COMMENTS = select()
            .all()
            .from("Comments")
            .where("article_id = ?")
            .and("parent_comment_id = ?")
            .limitAndOffset();

    JdbcOutboundCommentRepository(JDBC jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isCommentExists(UUID commentID) {
        return jdbc.readObjectOf(IS_COMMENT_EXISTS, Integer.class, commentID.toString())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking comment exists.");
                    return false;
                });
    }

    @Override
    public Result<CommentInfo, Throwable> commentInfo(UUID commentID) {
        return jdbc.read(COMMENT_INFO, this::commentInfoMapper, commentID.toString());
    }

    @Override
    public Result<Comment, Throwable> comment(UUID commentID) {
        return jdbc.read(COMMENT, this::commentMapper, commentID.toString());
    }

    @Override
    public Result<List<Comment>, Throwable> page(UUID articleID, int limit, int offSet) {
        return jdbc.readListOf(COMMENTS, this::commentMapper, articleID.toString(), limit, offSet);
    }

    @Override
    public Result<List<Comment>, Throwable> page(UUID articleID, UUID parentCommentID, int limit, int offSet) {
        return jdbc.readListOf(CHILD_COMMENTS, this::commentMapper, articleID.toString(), parentCommentID.toString(), limit, offSet);
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
                UUID.fromString(rs.getString("user_id"))
        );

        return new Comment(commentIdentifiers, new CommentText(rs.getString("text")), reference);
    }
}
