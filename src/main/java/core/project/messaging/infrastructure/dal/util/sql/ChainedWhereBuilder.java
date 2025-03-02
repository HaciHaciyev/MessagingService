package core.project.messaging.infrastructure.dal.util.sql;

public class ChainedWhereBuilder {
    private final StringBuilder query;

    ChainedWhereBuilder(StringBuilder query) {
        this.query = query;
    }

    public ChainedWhereBuilder and(String condition) {
        query.append("AND ").append(condition).append(" ");
        return this;
    }

    public ChainedWhereBuilder or(String condition) {
        query.append("OR ").append(condition).append(" ");
        return this;
    }

    public GroupByBuilder groupBy(String... columns) {
        query.append("GROUP BY ").append(String.join(", ", columns)).append(" ");
        return new GroupByBuilder(query);
    }

    public GroupByBuilder groupByf(String condition) {
        query.append("GROUP BY ").append(condition).append(" ");
        return new GroupByBuilder(query);
    }

    public OrderByBuilder orderBy(String column, Order order) {
        query.append("ORDER BY ").append(column).append(" ").append(order).append(" ");
        return new OrderByBuilder(query);
    }

    public OrderByBuilder orderBy(String customOrder) {
        query.append("ORDER BY ").append(customOrder).append(" ");
        return new OrderByBuilder(query);
    }

    public String limitAndOffset() {
        query.append("LIMIT ").append("? ").append("OFFSET ").append("? ");
        return this.query.toString();
    }

    public String limitAndOffset(int limit, int offset) {
        query.append("LIMIT ").append(limit).append(" ").append("OFFSET ").append(offset).append(" ");
        return this.query.toString();
    }

    public String build() {
        return this.query.toString();
    }
}
