package io.openvidu.call.java.services;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import com.google.firebase.messaging.Notification;
import io.openvidu.call.java.models.AppNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    @Autowired
    Firestore db;

    @PostMapping("/send-notification")
    public ResponseEntity<?> sendNotification(@RequestBody(required = false) Map<String, ?> params) throws IOException {

        // Get authentication token for phone number
      String phoneNumber= (String) params.get("phoneNumber");

        try {
          DocumentReference docRef = db.collection("userdata").document(phoneNumber);
          ApiFuture<DocumentSnapshot> future = docRef.get();
          DocumentSnapshot document = future.get();
          AppNotification appNotification=new AppNotification();
          if (document.exists()) {
            looger.info("Document data: " + document.getData());
            appNotification=document.toObject(AppNotification.class);
          } else {
            System.out.println("No such document!");
          }


            looger.info(appNotification.getUsertoken());

            // Create notification message
            //Notification notification = new Notification("New message received", "You have a new message");
            HashMap<String,String> response=new HashMap<>();
            Notification notification = Notification
                    .builder()
                    .setTitle("Join Call")
                    .setBody("Please join the call...")
                    .build();
            Message message = Message.builder()
                    .setToken(appNotification.getUsertoken())
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


