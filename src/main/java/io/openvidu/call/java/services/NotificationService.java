package io.openvidu.call.java.services;

import com.google.gson.Gson;
//import io.openvidu.call.java.controllers.URLController;
import io.openvidu.call.java.models.ShortenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.mail.*;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/notify")
public class NotificationService {
    @Autowired
    RestTemplate restTemplate;
    @Value("${sms.url:-}")
    String url;
    @Value("${sms.text:-}")
    String text;
  //  @Autowired
  //  URLController urlController;
    @Autowired
    ShortenRequest shortenRequest;
    @Value("${tiny.url:-}")
    private String baseString;

    @PostMapping("/sms")
    public ResponseEntity<?> sendSms(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request,
                                         HttpServletResponse res) throws Exception {
        String msisdn = params.get("msisdn").toString();
        String CallUrl = params.get("callURl").toString();
        Gson gson = new Gson();
        HttpHeaders headers = new HttpHeaders();
        shortenRequest.setUrl(CallUrl);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity<String>(headers);
        String finalUR=baseString+CallUrl;
        System.out.println(finalUR+baseString+CallUrl);
        String finalText=createURL(text,finalUR);
        finalText= URLEncoder.encode(finalText, "UTF-8");
        String finalUrl=createURL(url,msisdn,finalText);
        URI uri =new URI(finalUrl);

        String responseBody= restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();

        System.out.println(responseBody);
        Map<Object,Object> attributes = gson.fromJson(responseBody,Map.class);
        Map<String, Object> response = new HashMap<String, Object>();
        System.out.println("Response url:- "+baseString+CallUrl);
        response.put("state",attributes.get("state"));
        response.put("description",attributes.get("description"));
        response.put("Url",baseString+CallUrl);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request,
                                         HttpServletResponse res) throws Exception {
        String msisdn = params.get("email").toString();
        String CallUrl = params.get("callURl").toString();
        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();
        System.out.println("Properties "+properties);

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication("shreyavats18@gmail.com","nuooxogkjixpupik");
            }
        });

        MimeMessage mess = new MimeMessage(session);

        try{
            mess.setFrom("shreyavats18@gmail.com");
            mess.setRecipients(Message.RecipientType.TO, InternetAddress.parse(msisdn));
            mess.setSubject("Join the call");
            mess.setText(CallUrl);

            Transport.send(mess);

        }
        catch(Exception e){
            e.printStackTrace();
        }
        Map<String, Object> response = new HashMap<String, Object>();
        //response.put("Url",baseString+CallUrl);
        return new ResponseEntity<>(response,HttpStatus.OK);

    }


    public String createURL (String url, Object ... params) {
        return new MessageFormat(url).format(params);
    }

}
