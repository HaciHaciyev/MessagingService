package core.project.messaging.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.project.messaging.infrastructure.utilities.json.JsonUtilities;
import io.quarkus.logging.Log;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Message(MessageType type, String message, String partner) {

    private static final ObjectMapper mapper = new ObjectMapper();

    public Message  {
        Objects.requireNonNull(type, "type cannot be null");
    }

    public static Message error(String message) {
        return new Message(MessageType.ERROR, message, null);
    }

    public static Message info(String message) {
        return new Message(MessageType.INFO, message, null);
    }

    public static Message userInfo(String message) {
        return new Message(MessageType.USER_INFO, message, null);
    }


    @Override
    public String toString() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.error(e);
        }
        return "";
    }


    public String asJSON() {
        return JsonUtilities.writeJSON(this);
    }
}
