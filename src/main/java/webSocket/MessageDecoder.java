package webSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class MessageDecoder implements Decoder.Text<Message> {
    private static Gson gson = new Gson();

    @Override
    public Message decode(String s) {
        JsonObject message = JsonParser.parseString(s).getAsJsonObject();
        System.out.println(message);
        String type = message.get("type").getAsString();
        switch(type) {
            case "join":
                return gson.fromJson(s, MessageJoin.class);
            default:
                throw new RuntimeException("Type of message not recognized: " + type);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
