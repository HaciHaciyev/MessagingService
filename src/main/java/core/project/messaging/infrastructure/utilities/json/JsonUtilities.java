package core.project.messaging.infrastructure.utilities.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.project.messaging.application.dto.Message;
import core.project.messaging.application.dto.MessageType;
import core.project.messaging.domain.value_objects.Username;
import core.project.messaging.infrastructure.utilities.containers.Result;


public class JsonUtilities {

    private JsonUtilities() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Result<JsonNode, Throwable> jsonTree(final String message) {
        try {
            return Result.success(objectMapper.readTree(message));
        } catch (JsonProcessingException e) {
            return Result.failure(e);
        }
    }

    public static Result<MessageType, Throwable> messageType(final String message) {
        Result<JsonNode, Throwable> resultNode = jsonTree(message);
        if (!resultNode.success()) {
            return Result.failure(resultNode.throwable());
        }

        return Result.ofThrowable(() -> MessageType.valueOf(resultNode.value().get("type").asText()));
    }


    public static Result<String, Throwable> message(JsonNode messageNode) {
        return Result.ofThrowable(() -> messageNode.get("message").asText());
    }

    public static Result<Message, Throwable> messageRecord(JsonNode messageNode) {
        return Result.ofThrowable(() -> new Message(messageNode.get("message").asText()));
    }

    public static Result<Username, Throwable> usernameOfPartner(JsonNode messageNode) {
        return Result.ofThrowable(() -> new Username(messageNode.get("partner").asText()));
    }

}
