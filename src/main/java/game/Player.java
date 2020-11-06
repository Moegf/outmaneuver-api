package game;

import javax.websocket.Session;
import java.util.Optional;

public class Player {
    private String uid;
    private Session session;
    private Optional<Room> room;

    public String getUid() { return uid; }
    public Session getSession() { return session; }

    public Player(String uid, Session session){
        this.uid = uid;
        this.session = session;
        room = Optional.empty();
    }

    public Player(String uid, Session session, Room room){
        this(uid, session);
        this.room = Optional.of(room);
    }

    //removes player from room, if room exists
    public void leaveRoom() {
        room.ifPresent(room -> room.removePlayer(this));
        room = Optional.empty();
    }

    @Override
    public String toString() {
        return "(Player: " + uid + (room.isPresent()? room : "") + ")";
    }
}
