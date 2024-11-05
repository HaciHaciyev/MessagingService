package core.project.messaging.infrastructure.utilities.repository;

import core.project.messaging.infrastructure.exceptions.RepositoryDataException;
import jakarta.annotation.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {
    @Nullable
    T extractData(ResultSet rs, int rowNum) throws SQLException, RepositoryDataException;
}
