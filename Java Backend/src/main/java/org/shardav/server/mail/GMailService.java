package org.shardav.server.mail;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.shardav.utils.Log;

public class GMailService {
    
    private static final String APPLICATION_NAME = "Chat Server";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static final String LOG_TAG = GMailService.class.getCanonicalName() + " : " + GMailService.class.getSimpleName();

    private static final Object LOCK = new Object();
    private static GMailService instance = null;

    private final NetHttpTransport HTTP_TRANSPORT;
    private final Gmail SERVICE;


    private GMailService() throws IOException, GeneralSecurityException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        SERVICE = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    private Credential getCredentials() throws IOException {
       
        //Load client secrets.
        InputStream in = GMailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null){
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        //Build flow and trigger authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                                                JSON_FACTORY, 
                                                clientSecrets, 
                                                SCOPES)
                                                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                                                .setAccessType("offline")
                                                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8889).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }
    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(bodyText,"text/html");

        return email;
    }

    private Message createMessageWithEmail(MimeMessage emailContent)throws IOException, MessagingException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);

        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        return message;
    }

    private void sendMessage(String userId, MimeMessage emailContent) throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        message = SERVICE.users().messages().send(userId, message).execute();

        Log.v(LOG_TAG, "Email sent with id: " + message.getId());
    }

    public void sendRegistrationOTP(String to, String OTP) throws IOException, MessagingException {

        String user = "me";

        String message = "" +
                "<div style=\"font-family:helvetica; font-size:200%\">" +
                "<p>" +
                "Welcome!" +
                "<br><br>" +
                "Here is your one time registration password" +
                "<br><br><br><br>" +
                "<span style=\"font-size:200%;background-color: blue; padding: 5px; color: white;\">"+OTP+"</span>" +
                "<br><br><br><br>" +
                "Regards,<br><br>" +
                "Team SocketChat" +
                "<br><br><br><br><br><br>" +
                "<span style=\"font-size:25%;\"><em>PS: This mail was auto generated by the system. Please do not reply.</em></span>" +
                "</p>" +
                "</div>";

        sendMessage(user, createEmail(to, user, "One Time Password", message));

    }

    public static GMailService getInstance() throws GeneralSecurityException, IOException {
        synchronized (LOCK) {
            if(instance == null) {
                instance = new GMailService();
            }
            return instance;
        }
    }

    // Not sure if this is needed waiting for server to break once.
    public void destroyInstance() {
        synchronized (LOCK) {
            if (instance != null) {
                instance = null;
            }
        }
    }

    //TODO (After doing everything else!!!): Add OTP message for password change.

}