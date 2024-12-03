package core.project.messaging.infrastructure.utilities.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.project.messaging.application.dto.Message;
import io.quarkus.logging.Log;


public class JsonUtilities {

    private JsonUtilities() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String writeJSON(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String writeMessage(Message message) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            Log.errorf("Can`t serialize message: %s", e.getMessage());
        }

        return "";
    }
}
