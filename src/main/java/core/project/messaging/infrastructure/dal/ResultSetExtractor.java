package core.project.messaging.infrastructure.dal;

import core.project.messaging.infrastructure.exceptions.RepositoryDataException;
import jakarta.annotation.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExtractor<T> {
    @Nullable
    T extractData(ResultSet rs) throws SQLException, RepositoryDataException;
}