package core.project.messaging.application.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.project.messaging.application.dto.Message;
import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


public class JSONUtilities {

    private JSONUtilities() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String writeJSON(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            Log.errorf("Can`t parse message: %s", e);
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

    public static WebApplicationException responseException(Response.Status status, String message) {
        return new WebApplicationException(Response.status(status).entity(message).type(MediaType.APPLICATION_JSON_TYPE).build());
    }
}
