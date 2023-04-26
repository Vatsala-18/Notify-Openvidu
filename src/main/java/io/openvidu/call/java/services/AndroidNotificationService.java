package io.openvidu.call.java.services;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FcmOptions;
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

    @Value("${firebase.collection}")
    private String firebaseCollection;
    @Autowired
    FirebaseMessaging firebaseMessaging;

    @Autowired
    FirebaseAuth firebaseAuth;
    @Autowired
    Firestore db;

    @PostMapping("/sendNotification")
    public ResponseEntity<?> sendNotification(@RequestBody(required = false) Map<String, ?> params) throws IOException {

        // Get authentication token for phone number
        String phoneNumber= (String) params.get("msisdn");
        String sessionId = (String) params.get("sessionId");

        try {
            DocumentReference docRef = db.collection(firebaseCollection).document(phoneNumber);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            AppNotification appNotification=new AppNotification();
            if (document.exists()) {
                appNotification=document.toObject(AppNotification.class);
            } else {
                System.out.println("No such document!");
            }
            // Create notification message
            //Notification notification = new Notification("New message received", "You have a new message");
            HashMap<String,String> response=new HashMap<>();
          /*  Notification notification = Notification
                    .builder()
                    .setTitle("Join Call")
                    .setBody("Please join the call...")
                    .build();*/
            HashMap<String, String>map= new HashMap<>();
            map.put("TITLE","mCarbon Support");
            map.put("SESSION_ID",sessionId);
            map.put("BODY","Please join video call");

            Message message = Message.builder()
                    .setToken(appNotification.getUsertoken())
                    .putAllData(map)
                    .build();

            // Send notification message
            String id=firebaseMessaging.send(message);
            response.put("id",id);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            looger.error("Getting Exception While Submitting the message {}",e.getStackTrace());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

