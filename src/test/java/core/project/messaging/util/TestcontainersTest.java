package core.project.messaging.util;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(ChesslandTestResource.class)
@QuarkusTest
public class TestcontainersTest {

    @Test
    public void testPostgres() {
        System.out.println("correctness test");
    }
}
