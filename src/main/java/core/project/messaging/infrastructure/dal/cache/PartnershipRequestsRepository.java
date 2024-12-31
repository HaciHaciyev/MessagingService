package core.project.messaging.infrastructure.dal.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import core.project.messaging.infrastructure.utilities.containers.StatusPair;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class PartnershipRequestsRepository {

    private static final String KEY_FORMAT = "Partnership requests {%s}";

    private final HashCommands<String, String, String> hashCommands;

    PartnershipRequestsRepository(RedisDataSource redisDataSource) {
        this.hashCommands = redisDataSource.hash(new TypeReference<>(){});
    }

    public void put(String addressee, String addresser, String message) {
        hashCommands.hset(String.format(KEY_FORMAT, addressee), addresser, message);
    }

    public StatusPair<String> get(String addressee, String addresser) {
        String message = hashCommands.hget(String.format(KEY_FORMAT, addressee), addresser);
        if (Objects.nonNull(message)) {
            return StatusPair.ofTrue(message);
        }

        return StatusPair.ofFalse();
    }

    public Map<String, String> getAll(String addressee) {
        return hashCommands.hgetall(String.format(KEY_FORMAT, addressee));
    }

    public StatusPair<String> poll(String addressee, String addresser) {
        String message = hashCommands.hget(String.format(KEY_FORMAT, addressee), addresser);
        if (Objects.nonNull(message)) {
            hashCommands.hdel(addressee, addresser);
            return StatusPair.ofTrue(message);
        }

        return StatusPair.ofFalse();
    }

    public Map<String, String> pollAll(String addressee) {
        Map<String, String> result = hashCommands.hgetall(String.format(KEY_FORMAT, addressee));
        hashCommands.hdel(String.format(KEY_FORMAT, addressee));
        return result;
    }

    public void delete(String addressee, String addresser) {
        hashCommands.hdel(String.format(KEY_FORMAT, addressee), addresser);
    }
}