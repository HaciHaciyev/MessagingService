package core.project.messaging.infrastructure.security;

import core.project.messaging.domain.entities.UserAccount;
import core.project.messaging.infrastructure.utilities.containers.Result;
import io.quarkus.logging.Log;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;
import jakarta.websocket.Session;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class JwtUtility {

    private final JWTParser jwtParser;

    public JwtUtility(JWTParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    public String generateToken(UserAccount userAccount) {
        Log.info("New token generation.");

        Duration expiration = Duration.ofDays(1).plusSeconds(1);
        return Jwt.issuer("Chessland")
                .upn(userAccount.getUsername().username())
                .groups(userAccount.getUserRole().getUserRole())
                .expiresIn(expiration)
                .sign();
    }

    public String refreshToken(UserAccount userAccount) {
        Log.info("New token generation.");

        Duration year = Duration.ofDays(365);
        return Jwt.issuer("Chessland")
                .upn(userAccount.getUsername().username())
                .groups(userAccount.getUserRole().getUserRole())
                .expiresIn(year)
                .sign();
    }

    public Optional<JsonWebToken> extractJWT(final Session session) {
        final List<String> token = session.getRequestParameterMap().get("token");
        if (Objects.isNull(token)) {
            return Optional.empty();
        }

        if (token.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jwtParser.parse(token.getFirst()));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }
}