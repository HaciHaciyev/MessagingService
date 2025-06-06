package core.project.messaging.infrastructure.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.project.messaging.application.dto.messaging.Message;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class MessageDecoder implements Decoder.Text<Message> {

    private static final int MAX_LENGTH = 512;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Message decode(String json) throws DecodeException {
        if (json == null || json.length() > MAX_LENGTH) {
            throw new DecodeException(json, "Invalid message: null or exceeds 512 characters");
        }

        try {
            return objectMapper.readValue(json, Message.class);
        } catch (Exception e) {
            throw new DecodeException(json, "Unable to decode JSON", e);
        }
    }

    @Override
    public boolean willDecode(String json) {
        return json != null && json.length() <= MAX_LENGTH;
    }
}

