package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Random;


public class SignUpHandler {

    private final ClientHandler client;
    private String otp;
    private final Gson gson;
    private UserDetails userDetails;
    private GMailService mailService;
    private Database database;

    private static final String LOG_TAG = SignUpHandler.class.getCanonicalName();

    public SignUpHandler(ClientHandler client) {
        this.client = client;
        this.userDetails = null;
        this.gson = new Gson();
        try {
            this.mailService = GMailService.getInstance();
            this.database = Database.getInstance();
        } catch (GeneralSecurityException | IOException | InstantiationException ignore) {
        }
    }

    protected void handleJson(JsonObject root) {
        String request = root.getAsJsonPrimitive("request").getAsString();
        if ("registration".equals(request)) {
            handleRegistration(root);
        } else {
            verifyOTP(root.has("otp") ? root.getAsJsonPrimitive("otp").getAsString() : null);
        }
    }

    private void handleRegistration(JsonObject root) {
        if (root.has("details")) {
            this.userDetails = gson.fromJson(root.get("details"), UserDetails.class);
            String email = userDetails.getEmail();
            String username = userDetails.getUsername();
            String password = userDetails.getPassword();

            if (email != null && username != null && password != null) {
                ServerExecutors.getDatabaseResultExecutor().submit(()->{
                    try {
                        UserDetails emailDetails = database.fetchUserDetailsByMail(email);
                        UserDetails usernameDetails = database.fetchUserDetailsByUsername(username);
                        if (emailDetails == null && usernameDetails == null) {
                            StringBuilder otp = new StringBuilder(new Random(new Random().nextInt(Integer.MAX_VALUE)).nextInt(10000));
                            while (otp.length() < 4) {
                                otp.insert(0, '0');
                            }
                            ServerExecutors.getOtpExecutor().submit(()->sendOTP(otp.toString()));
                        } else {
                            String message = String.format("User with %s %s already exists",
                                    emailDetails == null ? "username" : "email",
                                    emailDetails == null ? username : email);
                            Response invalid = new Response(ResponseStatus.INVALID, message);
                            client.out.println(gson.toJson(invalid));
                            client.out.flush();
                        }
                    } catch (SQLException ignore) {}
                });
            } else {
                this.userDetails = null;
                Response invalid = new Response(ResponseStatus.INVALID, "User details not present");
                client.out.println(gson.toJson(invalid));
                client.out.flush();
            }

        } else {
            Response invalid = new Response(ResponseStatus.INVALID, "User details not present");
            client.out.println(gson.toJson(invalid));
            client.out.flush();
        }
    }

    private void sendOTP(String otp) {
        this.otp = otp;
        Response response = new Response(ResponseStatus.FAILED);
        try {
            mailService.sendRegistrationOTP(userDetails.getEmail(), otp);
            response.setResponseStatus(ResponseStatus.SENT);
            response.setMessage("OTP sent to " + userDetails.getEmail());
        } catch (MessagingException | IOException ex) {
            Log.e(LOG_TAG, "An error occurred while trying to send OTP " + ex.getMessage());
            response.setMessage(ex.getMessage());
        }
        client.out.println(gson.toJson(response));
        client.out.flush();
    }

    private void verifyOTP(String otp) {
        if (this.otp != null && this.userDetails != null) {
            if (otp != null) {
                if (this.otp.equals(otp)) {
                    try {
                        database.insertUser(userDetails);
                        client.out.println(gson.toJson(new Response(ResponseStatus.SUCCESS)));
                        client.out.flush();
                        this.userDetails = null;
                        this.otp = null;
                    } catch (SQLException ex) {
                        Log.e(LOG_TAG, "Error inserting user" + userDetails.getEmail());
                        Response errorResponse = new Response(ResponseStatus.FAILED,
                                "Error inserting user :" + ex.getLocalizedMessage());
                        client.out.println(gson.toJson(errorResponse));
                        client.out.flush();
                    }
                } else {
                    Response response = new Response(ResponseStatus.FAILED, "Wrong OTP");
                    client.out.println(gson.toJson(response));
                    client.out.flush();
                }
            } else {
                client.out.println(gson.toJson(new Response(ResponseStatus.FAILED, "Key OTP not present")));
                client.out.flush();
            }
        } else {
            Response invalid = new Response(ResponseStatus.INVALID, "Make a registration request first");
            client.out.println(gson.toJson(invalid));
            client.out.flush();
        }
    }

}
