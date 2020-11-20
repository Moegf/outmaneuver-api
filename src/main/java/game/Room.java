package game;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import firebase.Firebase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webSocket.MessageOutgoing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Room {

    private static final Logger logger = LoggerFactory.getLogger(Room.class);

    public static final Map<String, Room> roomByID = new HashMap<>();

    static {
        logger.debug("Instantiating Room");
        try {
            loadFromFirebase();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public enum Role {
        WAITING(6, "waiting"),
        CONGLOMERATE_PILOT(3, "conglomerate pilot"),
        REBEL_PILOT(1, "rebel pilot"),
        NAVIGATOR(1, "navigator"),
        ENGINEER(1, "engineer");

        //for efficiency create a map from string to role (constant time is better then linear)
        private static final Map<String, Role> stringToRole = new ConcurrentHashMap<>();

        public static final Role roleFromString(String role){
            Role result = stringToRole.get(role);
            if(result == null)
                throw new IllegalArgumentException("Unable to find a role with text representation: " + role);
            return result;
        }

        static {
            for(Role role: values())
                stringToRole.put(role.name, role);
        }

        private final int maxPlayers;
        private final String name;

        Role(int maxPlayers, String name) {
            this.maxPlayers = maxPlayers;
            this.name = name;
        }

        public int getMaxPlayers() { return maxPlayers; }
        public String getName() { return name; }
    }

    private final Map<Player, Role> playersToRole;
    private final String host;
    private final String id;
    //TODO: status should be changed to be an enum
    private final String status;

    private final Map<Role, Integer> roleAvailability;

    public String getID() { return id; }

    public Room(String id, String host, String status, Map<Player, Role> playerToRole){
        this.playersToRole = playerToRole;
        this.id = id;
        this.host = host;
        this.status = status;

        roleAvailability = new ConcurrentHashMap<>();

        for(Role role: Role.values())
            roleAvailability.put(role, role.getMaxPlayers());
    }

    public Room(String id, String host, String status){
        this(id, host, status, new ConcurrentHashMap<>());
    }

    public void addPlayer(Player player, Role role) {
        playersToRole.put(player, role);
        logger.debug("Player: " + player + " has joined room: " + this);
        roleAvailability.put(role, roleAvailability.get(role) - 1);
        updateFirestore();

        //send gamedata to player
        sendGameData(player);
    }

    public void addPlayer(Player player) { addPlayer(player, Role.WAITING);}

    public void removePlayer(Player player) {
        Role role = playersToRole.remove(player);
        roleAvailability.put(role, roleAvailability.get(role) + 1);
        logger.debug("Player: " + player + " has left room: " + this);
        //delete doc in firebase
        try {
            Firebase.getDB().collection("rooms").document(id).collection("players").document(player.getUid()).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //update other users
        sendGameData();
    }

    public void updateFirestore() {
        Firestore db = firebase.Firebase.getDB();

        //add basic data
        db.collection("rooms").document(id).set(this, SetOptions.merge());

        //add players
        CollectionReference playersReference = db.collection("rooms").document(id).collection("players");

        for(Map.Entry<Player, Role> entry: playersToRole.entrySet()) {
            //update player roles
            playersReference.document(entry.getKey().getUid()).set(
                Map.ofEntries(
                    Map.entry("role", entry.getValue())
            ), SetOptions.merge());
        }

        logger.info("Updated firestore copy of room: " + this);
    }

    //send game data to player
    public void sendGameData(Player player) {
        logger.info("Sending game data for room: " + this + " to " + player);

        MessageOutgoing message = new MessageOutgoing.Builder("gamedata")
                .put("players", playersToRole.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getUid(), e -> e.getValue().getName())))
                .put("roles", roleAvailability.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getName(), e -> e)))
                .put("status", status)
                .build();

        //send data to player
        player.send(message);
    }

    //send game data to a collection of players
    public void sendGameData(Collection<Player> players){
        for(Player player: players)
            sendGameData(player);
    }

    //send game data to all players in the room
    public void sendGameData() { sendGameData(playersToRole.keySet()); }

    //sets the role of a given player
    public void setRole(Player player, Role role) {
        if(!playersToRole.keySet().contains(player))
            throw new IllegalArgumentException("Player " + player + " is not in room " + this);

        playersToRole.put(player, role);
        roleAvailability.put(role, roleAvailability.get(role) - 1);

        logger.info("The player " + player + " has chosen the role " + role);

        updateFirestore();
        sendGameData();
    }

    public static void addRoom(Room room){ roomByID.put(room.getID(), room); }

    public static void removeRoom(Room room){ roomByID.remove(room.getID()); }

    public static Room getRoomById(String id) { return Room.roomByID.get(id); }

    public static void loadFromFirebase() throws ExecutionException, InterruptedException {

        logger.info("Loading rooms from firestore");

        Firestore db = firebase.Firebase.getDB();

        ApiFuture<QuerySnapshot> future =  db.collection("rooms").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for(DocumentSnapshot document: documents){

            String id = document.getId();
            String host = document.getString("host");
            String status = document.getString("status");
            Map<Player, Role> playersToRole = new ConcurrentHashMap<>();

            //get players
            ApiFuture<QuerySnapshot> playerFuture = db.collection("room").document(id).collection("players").get();
            List<QueryDocumentSnapshot> playerDocuments = playerFuture.get().getDocuments();

            //for(DocumentSnapshot playerDocument: playerDocuments)
            //    playersToRole.put(playerDocument.getId(), Role.roleFromString(playerDocument.getString("role")));

            Room room = new Room(id, host, status, playersToRole);

            //TODO: implement offline players
            addRoom(room);
            logger.debug("Room loaded: " + room);
        }

        logger.info("Rooms loaded");

        //add listener for update
        db.collection("rooms").addSnapshotListener((snapshots, e) -> {
            if(e != null)
                throw e;

            logger.debug("Updating Rooms");

            //create a new empty map
            Map<String, Room> newRooms = new HashMap<>();

            for(DocumentSnapshot document: snapshots) {
                //check if a room with the id already exists
                String id = document.getId();
                if(roomByID.containsKey(id)){
                    Room room = roomByID.remove(id);
                    newRooms.put(id, room);
                } else {
                    String host = document.getString("host");
                    String status = document.getString("status");
                    Room room = new Room(id, host, status);
                    newRooms.put(id, room);
                    logger.debug("A new room was added: " + room);
                }
            }

            //update all players in new room
            newRooms.forEach((string, room) -> room.sendGameData());

            logger.info("The following rooms were removed: " + roomByID.values());

            roomByID.clear();
            roomByID.putAll(newRooms);
        });
    }

    @Override
    public String toString(){
        return "(Room: " + id + ", status: " + status + ")";
    }

}
