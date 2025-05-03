package core.project.messaging.infrastructure.dal.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import core.project.messaging.domain.commons.containers.StatusPair;
import core.project.messaging.domain.user.repositories.PartnershipRequestsRepository;
import core.project.messaging.domain.user.value_objects.Username;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class RedisPartnershipRequestsRepository implements PartnershipRequestsRepository {

    private static final String KEY_FORMAT = "Partnership requests {%s}";

    private final HashCommands<String, String, String> hashCommands;

    RedisPartnershipRequestsRepository(RedisDataSource redisDataSource) {
        this.hashCommands = redisDataSource.hash(new TypeReference<>(){});
    }

    @Override
    public void put(Username addressee, Username addresser, String message) {
        hashCommands.hset(String.format(KEY_FORMAT, addressee.username()), addresser.username(), message);
    }

    @Override
    public StatusPair<String> get(Username addressee, Username addresser) {
        String message = hashCommands.hget(String.format(KEY_FORMAT, addressee.username()), addresser.username());
        if (Objects.nonNull(message)) {
            return StatusPair.ofTrue(message);
        }

        return StatusPair.ofFalse();
    }

    @Override
    public Map<String, String> getAll(Username addressee) {
        return hashCommands.hgetall(String.format(KEY_FORMAT, addressee.username()));
    }

    @Override
    public void delete(Username addressee, Username addresser) {
        hashCommands.hdel(String.format(KEY_FORMAT, addressee.username()), addresser.username());
    }
}