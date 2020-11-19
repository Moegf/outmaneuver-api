package webSocket;

import javax.websocket.Session;

public class MessageRoom implements Message {
    private String name;
    private Session session;



    @Override
    public void resolve(Session session) {

    }
}
