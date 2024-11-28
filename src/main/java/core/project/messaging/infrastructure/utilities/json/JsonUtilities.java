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

    public static String writeJSON(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "";
    }
}
