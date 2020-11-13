package webSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that represents and outgoing message
 */
public class MessageOutgoing {
    private static final Gson gson = new Gson();

    private final String json;

    private MessageOutgoing(String json) {
        this.json = json;
    }

    public String getJson() { return json; }

    public static class Builder {
        private final Map<String, Object> data;

        public Builder(String type){
            data = new ConcurrentHashMap<>();
            data.put("type", type);
        }

        public Builder put(String key, Object value){
            data.put(key, value);
            return this;
        }

        public MessageOutgoing build() { return new MessageOutgoing(gson.toJson(data)); }
    }

}