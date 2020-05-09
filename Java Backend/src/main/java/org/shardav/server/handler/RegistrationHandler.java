package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.shardav.server.Server;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;


public class RegistrationHandler {

    private final ClientHandler client;
    private final Gson gson;

    private final Database database;
    private final GMailService mailService;

    private String otp;
    private UserDetails userDetails;

    private static final String LOG_TAG = RegistrationHandler.class.getCanonicalName();

    protected RegistrationHandler(ClientHandler client, Database database, GMailService mailService) {
        this.client = client;
        this.userDetails = null;
        this.gson = new Gson();
        this.database = database;
        this.mailService = mailService;
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
                            Response<Void> invalid = new Response<>(ResponseStatus.invalid, ResponseType.registration, message);
                            client.out.println(gson.toJson(invalid));
                            client.out.flush();
                        }
                    } catch (SQLException ignore) {}
                });
            } else {
                this.userDetails = null;
                Response<Void> invalid = new Response<>(ResponseStatus.invalid, ResponseType.registration, "User details not present");
                client.out.println(gson.toJson(invalid));
                client.out.flush();
            }

        } else {
            Response<Void> invalid = new Response<>(ResponseStatus.invalid, ResponseType.registration, "User details not present");
            client.out.println(gson.toJson(invalid));
            client.out.flush();
        }
    }

    private void sendOTP(String otp) {
        this.otp = otp;
        Response<Void> response = new Response<>(ResponseStatus.failed, ResponseType.registration);
        try {
            mailService.sendRegistrationOTP(userDetails.getEmail(), otp);
            response.setStatus(ResponseStatus.sent);
            response.setType(ResponseType.otp);
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
                    ServerExecutors.getDatabaseExecutor().submit(()->{
                        try {
                            database.insertUser(userDetails);
                            client.out.println(gson.toJson(new Response<Void>(ResponseStatus.success, ResponseType.verify)));
                            client.out.flush();
                            Server.CLIENT_MAP.put(userDetails.getEmail(), null);
                            userDetails.setPassword(null);
                            Server.CLIENT_SET.add(userDetails);
                            this.userDetails = null;
                            this.otp = null;
                        } catch (SQLException ex) {
                            Log.e(LOG_TAG, "Error inserting user" + userDetails.getEmail());
                            Response<Void> errorResponse = new Response<>(ResponseStatus.failed,
                                    ResponseType.verify,
                                    "Error inserting user :" + ex.getLocalizedMessage());
                            client.out.println(gson.toJson(errorResponse));
                            client.out.flush();
                        }
                    });
                } else {
                    Response<Void> response = new Response<>(ResponseStatus.failed, ResponseType.verify, "Wrong OTP");
                    client.out.println(gson.toJson(response));
                    client.out.flush();
                }
            } else {
                client.out.println(gson.toJson(new Response<Void>(ResponseStatus.failed, ResponseType.verify, "Key OTP not present")));
                client.out.flush();
            }
        } else {
            Response<Void> invalid = new Response<>(ResponseStatus.invalid, ResponseType.verify, "Make a registration request first");
            client.out.println(gson.toJson(invalid));
            client.out.flush();
        }
    }

}
