package core.project.messaging.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import core.project.messaging.infrastructure.utilities.json.JsonUtilities;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Message(MessageType type, String message, String partner) {

    public Message  {
        Objects.requireNonNull(type, "Type cannot be null.");
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
        return JsonUtilities.writeMessage(this);
    }

    public String asJSON() {
        return JsonUtilities.writeJSON(this);
    }
}
