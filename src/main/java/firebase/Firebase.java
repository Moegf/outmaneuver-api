package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Firebase {
    private static boolean initialized = false;
    static Firestore db;

    static public void initialize() throws IOException {
        if(initialized)
            return;

        FirestoreOptions options = FirestoreOptions.getDefaultInstance().toBuilder()
                .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(System.getenv("firebase").getBytes(StandardCharsets.UTF_8))))
                .build();


        db = options.getService();

        initialized = true;

        return;
    }
}
