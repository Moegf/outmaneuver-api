package game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Optional;

public class Player {
    private static Logger logger = LoggerFactory.getLogger(Player.class);

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

    //sends a message to the player
    public void send(Object object){
        try {
            session.getBasicRemote().sendObject(object);
        } catch (IOException | EncodeException e) {
            logger.error(e.getMessage());
        }
    }

    public void setRole(Room.Role role){
        room.ifPresent(room -> room.setRole(this, role));
    }

    @Override
    public String toString() {
        return "(Player: " + uid + (room.isPresent()? " " + room : "") + ")";
    }
}
