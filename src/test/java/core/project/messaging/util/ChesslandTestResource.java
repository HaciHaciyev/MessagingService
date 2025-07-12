package core.project.messaging.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class ChesslandTestResource implements QuarkusTestResourceLifecycleManager {

    GenericContainer<?> chesslandContainer;

    @Override
    public Map<String, String> start() {
        chesslandContainer = new GenericContainer<>("aingrace/chessland:chessland")
                .withExposedPorts(9097);

        chesslandContainer.start();

        int port = chesslandContainer.getMappedPort(9097);
        String chesslandURL = "http://localhost:%s".formatted(port);
        return Map.of("chessland-url", chesslandURL);
    }

    @Override
    public void stop() {
        if (chesslandContainer != null) {
            chesslandContainer.stop();
            chesslandContainer = null;
        }
    }

    @Override
    public int order() {
        return 9;
    }
}
