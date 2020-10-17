package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Firebase {
    private static boolean initialized = false;

    static public void initialize() throws IOException {
        if(initialized)
            return;

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(System.getenv("firebase").getBytes(StandardCharsets.UTF_8))))
                .setDatabaseUrl("https://outmaneuver-cc274.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

        initialized = true;

        return;
    }
}
