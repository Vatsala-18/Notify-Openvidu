package io.openvidu.call.java.services;


import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import com.google.firebase.messaging.Notification;
import io.openvidu.call.java.models.appNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

@RestController
@RequestMapping("/send")
public class AndroidNotificationService {
    public static final Logger looger= LoggerFactory.getLogger(AndroidNotificationService.class);

    @Value("${firebase.config}")
    private String firebaseConfig;
    @Autowired
    FirebaseMessaging firebaseMessaging;

    @Autowired
    FirebaseAuth firebaseAuth;

    @PostMapping("/send-notification")
    public ResponseEntity<?> sendNotification(@RequestBody String phoneNumber) throws IOException {

        // Generate authentication token for phone number
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            DocumentReference documentReference =
                    dbFirestore.collection("userdata").document(phoneNumber);
            ApiFuture<DocumentSnapshot> future = documentReference.get();
            DocumentSnapshot document = future.get();
            looger.info(String.valueOf(document.getData()));
            appNotification appNotification1=null;
            appNotification1=document.toObject(appNotification.class);

            String authToken=appNotification1.getUsertoken();
            looger.info(authToken);

            // Create notification message
            //Notification notification = new Notification("New message received", "You have a new message");
            HashMap<String,String> response=new HashMap<>();
            Notification notification = Notification
                    .builder()
                    .setTitle("Join Call")
                    .setBody("Please join the call...")
                    .build();
            Message message = Message.builder()
                    .setToken(authToken)
                    .setNotification(notification)
                    .build();

            // Send notification message
            String id=firebaseMessaging.send(message);
            response.put("id",id);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            looger.error("Exceptiom {}",e);
            return null;
        }
    }
}

 /*   @PostMapping("/send-notification")
    public ResponseEntity<String> sendNotification(@RequestBody String phoneNumber) throws IOException {
        // Initialize Firebase Admin SDK with credentials from the JSON key file
        InputStream serviceAccount = getClass().getResourceAsStream("/google-service.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);

        // Generate authentication token for phone number
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String authToken = auth.createCustomToken(phoneNumber);

        // Create notification message
        Notification notification = new Notification("New message received", "You have a new message");
        Message message = Message.builder()
                .setToken(authToken)
                .setNotification(notification)
                .build();

        // Send notification message
        FirebaseMessaging.getInstance().send(message);

        return new ResponseEntity<>("Notification sent to " + phoneNumber, HttpStatus.OK);
    }

 */


