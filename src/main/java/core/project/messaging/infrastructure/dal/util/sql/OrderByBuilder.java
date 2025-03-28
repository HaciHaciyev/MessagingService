package core.project.messaging.infrastructure.dal.util.sql;

public class OrderByBuilder {
    private final StringBuilder query;

    OrderByBuilder(StringBuilder query) {
        this.query = query;
    }

    public String limitAndOffset(int limit, int offset) {
        query.append("LIMIT ").append(limit).append(" ").append("OFFSET ").append(offset).append(" ");
        return this.query.toString();
    }

    public String limitAndOffset() {
        query.append("LIMIT ").append("? ").append("OFFSET ").append("? ");
        return this.query.toString();
    }

    public String build() {
        return this.query.toString();
    }
}
