package webSocket;

import javax.websocket.Session;

public interface Message {
    public void resolve(Session session);
}
