package core.project.messaging.infrastructure.dal.util.sql;

public class ReturningBuilder {
    private final StringBuilder query;

    ReturningBuilder(StringBuilder query) {
        this.query = query;
    }

    public String returning(String... columns) {
        return this.query.append("RETURNING ").append(String.join(", ", columns)).append(" ").toString();
    }

    public String returning(String[] columns, String condition) {
        return this.query.append("RETURNING ").append(String.join(", ", columns)).append(" ").append(condition).append(" ").toString();
    }

    public String returningAll() {
        return this.query.append("RETURNING *").toString();
    }

    public String returningAll(String condition) {
        return this.query.append("RETURNING *").append(" ").append(condition).append(" ").toString();
    }

    public String build() {
        return this.query.toString();
    }
}
