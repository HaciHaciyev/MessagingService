package core.project.messaging.util;


import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    GenericContainer<?> postgresContainer;

    @Override
    public Map<String, String> start() {
        postgresContainer = new GenericContainer<>("postgres:latest")
                .withExposedPorts(5432)
                .withEnv("POSTGRES_USER", "root")
                .withEnv("POSTGRES_PASSWORD", "password")
                .withEnv("POSTGRES_DB", "chessrepository");

        postgresContainer.start();

        String host = postgresContainer.getHost();
        int port = postgresContainer.getMappedPort(5432);
        String jdbcURL = "jdbc:postgresql://%s:%s/chessrepository".formatted(host, port);
        return Map.of("flyway-url", jdbcURL, "datasource-url", jdbcURL);
    }

    @Override
    public void stop() {
        if (postgresContainer != null) {
            postgresContainer.stop();
            postgresContainer = null;
        }
    }
}
