package webSocket;

import game.Player;
import game.Room.Role;

import javax.websocket.Session;

public class MessageRole implements Message {
    private Role role;

    public MessageRole(String role) {
        this.role = Role.roleFromString(role);
    }
    @Override
    public void resolve(Session session) {
        Player player = RoomSocket.getPlayerBySession(session);
        player.setRole(role);
    }
}
