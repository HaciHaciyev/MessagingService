package core.project.messaging.infrastructure.security;

import core.project.messaging.domain.commons.containers.Result;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Singleton;
import jakarta.websocket.Session;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Objects;

@Singleton
public class JWTUtility {

    private final JWTParser jwtParser;

    JWTUtility(JWTParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    /**
     * Extracts a JWT token from the WebSocket session request parameters.
     * <p>
     * WARNING: This method does NOT validate whether the token is expired.
     * Callers must explicitly check the 'exp' claim to ensure the token is still valid.
     */
    public Result<JsonWebToken, IllegalStateException> extractJWT(Session session) {
        List<String> token = session.getRequestParameterMap().get("token");
        if (Objects.isNull(token)) return Result.failure(new IllegalStateException("Token is missing."));
        if (token.isEmpty()) return Result.failure(new IllegalStateException("Token is missing."));
        try {
            JsonWebToken jwt = jwtParser.parse(token.getFirst());
            return Result.success(jwt);
        } catch (ParseException e) {
            return Result.failure(new IllegalStateException("Token is missing or invalid."));
        }
    }
}