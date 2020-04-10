package org.shardav.server.handler;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.Server;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.comms.login.LoginRequest;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class VerificationHandler implements Runnable {

    private Socket client;
    private static final String LOG_TAG = VerificationHandler.class.getSimpleName();

    private final GMailService gMailService;

    public VerificationHandler(Socket client, GMailService gMailService) {
        this.client = client;
        this.gMailService = gMailService;
    }

    @Override
    public void run() {

        try {

            Response errorResponse = new Response(ResponseStatus.INVALID);

            final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            final PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            Log.i(LOG_TAG, "New client connected : " + client.getInetAddress());

            String json = in.readLine();

            System.out.println(json);

            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject root = new JSONObject(jsonParser);

            try {

                RequestType request = RequestType.getRequestType(root.getString("request"));

                if (request == RequestType.LOGIN || request == RequestType.REGISTRATION) {

                    try {

                        Log.i(LOG_TAG, "Handling " + request.getValue() + " request.");

                        LoginRequest loginRequest = LoginRequest.getInstance(root);

                        UserDetails details = loginRequest.getDetails();

                        String username = details.getUsername();
                        String email = details.getEmail();

                        Database database = Database.getInstance();
                        UserDetails existingUser = null;
                        try {
                            existingUser = database.fetchUserDetailsByMail(email);
                        } catch (IllegalArgumentException ignore) {
                            //Ignore Exception
                        }
                        if (request == RequestType.REGISTRATION) {
                            if (existingUser == null) {
                                ServerExecutors.getVerificationHandlerExecutor().submit(new RegistrationHandler(client, in, out, details, gMailService));
                            } else {
                                errorResponse.setMessage("User already exists, please login.");
                                out.println(errorResponse.toJSON());
                                out.flush();
                            }
                        } else {
                            Future<UserDetails> databaseUser = ServerExecutors.getDatabaseExecutor().submit(()->{
                                try {
                                    return email == null ? database.fetchUserDetailsByUsername(username) : database.fetchUserDetailsByMail(email);
                                } catch (SQLException ex) {
                                    Log.e(LOG_TAG, "Error fetching user details", ex);
                                    Response failed = new Response(ResponseStatus.FAILED, ex.getMessage());
                                    out.println(failed.toJSON());
                                    out.flush();
                                    return null;
                                }
                            });

                            try {
                                UserDetails registeredDetails = databaseUser.get();
                                if (registeredDetails != null) {

                                    boolean sameUsername = email == null ? username.equals(registeredDetails.getUsername())
                                            : email.equals(registeredDetails.getEmail());

                                    boolean samePassword = details.getPassword().equals(registeredDetails.getPassword());

                                    if (sameUsername && samePassword) {

                                        Log.v(LOG_TAG, "Client username: " + (username == null ? email : username));

                                        ClientHandler clientHandler = new ClientHandler(client, details.getUsername(), in, out);
                                        Log.i(LOG_TAG, String.format("Adding %s to active clients list", (username == null ? email : username)));
                                        Server.activeClientMap.put(clientHandler.getEmail(), clientHandler);
                                        ServerExecutors.getClientHandlerExecutor().submit(clientHandler);

                                        out.println(new Response(ResponseStatus.SUCCESS).toJSON());
                                    } else {
                                        Response failed = new Response(ResponseStatus.FAILED, "Wrong credentials");
                                        out.println(failed.toJSON());
                                    }
                                    out.flush();
                                }
                            } catch (InterruptedException | ExecutionException ex) {
                                Log.e(LOG_TAG, "An error occurred while trying to retrieve user details", ex);
                                Response error = new Response(ResponseStatus.FAILED, ex.getCause().getMessage());
                                out.println(error.toJSON());
                                out.flush();
                            }
                        }

                    } catch (IllegalArgumentException | JSONException ex) {
                        Log.e(LOG_TAG, "An error occurred while trying to handle the JSON", ex);
                        errorResponse.setMessage(ex.getMessage());
                        out.println(errorResponse.toJSON());
                        out.flush();
                    } catch (InstantiationException | SQLException ex) {
                        Log.e(LOG_TAG, "An error occurred while trying to access the database", ex);
                        errorResponse.setMessage("Internal server error. Please try again later.");
                        out.println(errorResponse.toJSON());
                        out.flush();
                    }
                } else {
                    errorResponse.setMessage("The first request should always be a login request.");
                    out.println(errorResponse.toJSON());
                    out.flush();
                }
            } catch (IllegalArgumentException ex) {
                Log.e(LOG_TAG, "IllegalArgumentException occurred", ex);
                errorResponse.setMessage("The first request should always be a login request.");
                out.println(errorResponse.toJSON());
                out.flush();
            }

        } catch (IOException ex) {
            Log.e(LOG_TAG, "IOException occurred: " + ex.getMessage(), ex);
        }

    }

}
