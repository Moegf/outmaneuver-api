package webSocket;

import game.Player;
import game.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;

public class MessageJoin implements Message {
    private static final Logger logger = LoggerFactory.getLogger(MessageJoin.class);

    String uid;
    String roomID;

    public MessageJoin(String uid, String roomID) {
        this.uid = uid;
        this.roomID = roomID;
    }

    @Override
    public void resolve(Session session) {
        //add player locally to room
        Room room = Room.getRoomById(roomID);
        Player player = new Player(uid, session, room);
        room.addPlayer(player);
        RoomSocket.addPlayerBySession(player);

        //update room on firestore
        room.updateFirestore();

        logger.debug("Player: " + player + " joined Room: " + room);
    }
}
