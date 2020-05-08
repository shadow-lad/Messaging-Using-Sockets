package org.shardav.server.handler;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.Server;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RegistrationHandler implements Runnable {

    private static final String LOG_TAG = Server.class.getSimpleName() + ":" + RegistrationHandler.class.getSimpleName();

    final private Socket client;
    final private BufferedReader in;
    final private PrintWriter out;
    final private UserDetails details;
    final private GMailService gMailService;

    private String OTP;

    public RegistrationHandler(final Socket client, final BufferedReader in, final PrintWriter out, final UserDetails details, GMailService gMailService) {

        this.client = client;
        this.in = in;
        this.out = out;
        this.details = details;
        this.gMailService = gMailService;

    }

    @Override
    public void run() {

        Response response = new Response(ResponseStatus.FAILED);

        StringBuilder OTP = new StringBuilder(String.valueOf(new Random().nextInt(10000)));
        while (OTP.length() < 4)
            OTP.append("0");

        try {
            gMailService.sendRegistrationOTP(details.getEmail(), OTP.toString());

            response.setResponseStatus(ResponseStatus.SENT);

            response.setMessage("OTP sent to " + details.getEmail());

            this.OTP = OTP.toString();
        } catch (MessagingException | IOException ex) {
            Log.e(LOG_TAG, "An error occurred while trying to send OTP " + ex.getMessage());
            response.setMessage(ex.getMessage());
        }

        out.println(response.toJSON());
        out.flush();

        if (response.getResponseStatus() == ResponseStatus.SENT) {
            login();
        }

    }

    private void login() {

        try {
            String json = in.readLine();

            if(json == null) {
                return;
            }

            JSONObject verify = new JSONObject(new JSONTokener(json));

            if (verify.has("request")
                    && RequestType.valueOf(verify.getString("request")) == RequestType.verify) {

                if (verify.has("otp")
                        && verify.getString("otp") != null) {

                    if (this.OTP.equals(verify.getString("otp"))) {

                        Future<Boolean> inserted;
                        Database database;
                        try {
                            database = Database.getInstance();
                            inserted = ServerExecutors.getDatabaseExecutor().submit(()->{
                                try {
                                    return database.insertUser(details);
                                } catch (SQLException ex) {
                                    Log.e(LOG_TAG, "Error inserting user", ex);
                                    return false;
                                }
                            });
                        } catch (InstantiationException ex) {
                            ex.printStackTrace();
                            return;
                        }
                        try {
                            if (inserted.get()) {
                                details.setPassword(null);
                                ClientHandler clientHandler = new ClientHandler(client, details.getEmail(), in, out);
                                Server.ACTIVE_CLIENT_MAP.put(clientHandler.getEmail(), clientHandler);
                                Server.CLIENTS.add(details);
                                ServerExecutors.getClientHandlerExecutor().submit(clientHandler);
                                Server.NON_CLIENT_SOCKETS.remove(client);
                                Response success = new Response(ResponseStatus.SUCCESS);
                                out.println(success.toJSON());
                                out.flush();
                            }
                        }  catch (InterruptedException | ExecutionException ex) {
                            Log.e(LOG_TAG, "An error occurred while checking if user was inserted into the database. Rolling back", ex);
                            Response failed = new Response(ResponseStatus.FAILED, "An error occurred while trying to register user details. Try again later.");
                            out.println(failed.toJSON());
                            out.flush();
                            ServerExecutors.getDatabaseExecutor().submit(()->{
                                try {
                                    database.deleteUserByEmail(details.getEmail());
                                } catch (SQLException ignore) {

                                }
                            });
                        }

                    } else {
                        Response failed = new Response(ResponseStatus.FAILED, "Invalid OTP");
                        out.println(failed.toJSON());
                        out.flush();

                    }

                } else {
                    Response errorResponse = new Response(ResponseStatus.INVALID, "Key OTP should be present");
                    out.println(errorResponse.toJSON());
                    out.flush();
                }

            } else {
                Response errorResponse = new Response(ResponseStatus.INVALID, "Excepted an request of type verify");
                out.println(errorResponse.toJSON());
                out.flush();

            }

        } catch (IOException ex) {
            Log.e(LOG_TAG, "Error Reading from " + details.getEmail() + "'s input stream", ex);
        }

    }
}
