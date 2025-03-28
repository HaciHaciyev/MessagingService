package core.project.messaging.infrastructure.dal.util.sql;

public class SQLBuilder {

    private SQLBuilder() {}

    public static SelectBuilder select() {
        return SelectBuilder.select();
    }

    public static SelectBuilder selectDistinct() {
        return SelectBuilder.selectDistinct();
    }

    public static SelectBuilder withAndSelect(String table, String subQuery) {
        return SelectBuilder.with(table, subQuery);
    }

    public static InsertBuilder insert() {
        return new InsertBuilder();
    }

    public static InsertBuilder withAndInsert(String table, String subQuery) {
        return InsertBuilder.with(table, subQuery);
    }

    public static UpdateBuilder update(String table) {
        return UpdateBuilder.update(table);
    }

    public static DeleteBuilder delete() {
        return new DeleteBuilder();
    }
}