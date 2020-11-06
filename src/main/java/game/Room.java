package game;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

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

    private enum Role {
        WAITING(6, "waiting"),
        CONGLOMERATE_PILOT(3, "conglomerate_pilot"),
        REBEL_PILOT(1, "rebel_pilot"),
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

        private int maxPlayers;
        private String name;

        Role(int maxPlayers, String name) {
            this.maxPlayers = maxPlayers;
            this.name = name;
        }
    }

    //contains data needed to upload player to firestore
    private static final class dbPlayer {
        public String uid;
        public Role role;

        dbPlayer(String uid, Role role) {
            this.uid = uid;
            this.role = role;
        }
    }

    private final Map<Player, Role> playersToRole;
    private final String host;
    private final String id;
    private final String status;

    public String getID() { return id; }

    public Room(String id, String host, String status, Map<Player, Role> playerToRole){
        this.playersToRole = playerToRole;
        this.id = id;
        this.host = host;
        this.status = status;
    }

    public Room(String id, String host, String status){
        this(id, host, status, new ConcurrentHashMap<>());
    }

    public void addPlayer(Player player, Role role) {
        playersToRole.put(player, role);
        logger.debug("Player: " + player + " has joined room: " + this);
        updateFirestore();
    }

    public void addPlayer(Player player) { addPlayer(player, Role.WAITING);}

    public void removePlayer(Player player) {
        playersToRole.remove(player);
        logger.debug("Player: " + player + " has left room: " + this);
        updateFirestore();
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

        logger.debug("Updated firestore copy of room: " + this);
    }

    public static void addRoom(Room room){ roomByID.put(room.getID(), room); }

    public static void removeRoom(Room room){ roomByID.remove(room.getID()); }

    public static Room getRoomById(String id) { return Room.roomByID.get(id); }

    public static void loadFromFirebase() throws ExecutionException, InterruptedException {

        logger.debug("Loading rooms from firestore");

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

        logger.debug("Rooms loaded");

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

            logger.debug("The following rooms were removed: " + roomByID.values());

            roomByID.clear();
            roomByID.putAll(newRooms);
        });
    }

    @Override
    public String toString(){
        return "(Room: " + id + ", status: " + status + ")";
    }

}
