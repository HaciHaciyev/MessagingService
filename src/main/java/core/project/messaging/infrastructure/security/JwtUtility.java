package core.project.messaging.infrastructure.security;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Singleton;
import jakarta.websocket.Session;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class JwtUtility {

    private final JWTParser jwtParser;

    public JwtUtility(JWTParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    public Optional<JsonWebToken> extractJWT(final Session session) {
        final List<String> token = session.getRequestParameterMap().get("token");
        if (Objects.isNull(token)) return Optional.empty();
        if (token.isEmpty()) return Optional.empty();

        try {
            return Optional.of(jwtParser.parse(token.getFirst()));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }
}