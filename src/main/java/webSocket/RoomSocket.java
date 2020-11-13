package webSocket;

import game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value="/roomsocket", encoders = {MessageEncoder.class}, decoders = {MessageDecoder.class})
public class RoomSocket {
    private static Logger logger = LoggerFactory.getLogger(RoomSocket.class);

    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static Map<Session, Player> playerBySession = new ConcurrentHashMap<>();

    public static void addPlayerBySession(Player player){
        playerBySession.put(player.getSession(), player);
    }
    public static Player getPlayerBySession(Session session) { return playerBySession.get(session); }

    @OnOpen
    public void onOpen(Session session) { sessions.add(session); }

    @OnMessage
    public void onMessage(Session session, Message message){
        message.resolve(session);
    }

    @OnClose
    public void onClose(Session session){
        sessions.remove(session);
        if(playerBySession.containsKey(session)) {
            Player player = playerBySession.remove(session);
            player.leaveRoom();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.debug("An Error Occurred with the Websocket, " + session + ": " + error.getMessage());
        sessions.remove(session);
        if(playerBySession.containsKey(session)) {
            Player player = playerBySession.remove(session);
            player.leaveRoom();
        }
    }
}
