package core.project.messaging.application.dto;

import java.util.Objects;

public record Message(String message) {

    public Message  {
        Objects.requireNonNull(message);

        if (message.isBlank()) {
            throw new IllegalArgumentException("Message can`t be blank.");
        }

        if (message.length() > 255) {
            throw new IllegalArgumentException("Message can`t be longer than 255 characters.");
        }
    }
}
