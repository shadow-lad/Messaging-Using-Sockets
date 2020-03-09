package org.shardav.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.login.LoginDetails;
import org.shardav.server.comms.login.LoginRequest;
import shardav.utils.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginHandler implements Runnable {

    private Socket client;
    private static final String LOG_TAG = LoginHandler.class.getSimpleName();

    private static final AtomicBoolean loggedIn = new AtomicBoolean(false);

    LoginHandler(Socket client){
        this.client = client;
    }

    @Override
    public void run() {

        try{

            Response errorResponse = new Response(ResponseStatus.INVALID);

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            Log.i(LOG_TAG, "New login request received from : " + client.getInetAddress());

            while(!loggedIn.get()) {

                String json = in.readLine();

                System.out.println(json);

                if(json == null)
                    continue;

                JSONTokener jsonParser = new JSONTokener(json);
                JSONObject root = new JSONObject(jsonParser);

                try {

                    RequestType request = RequestType.getRequestType(root.getString("request"));

                    if (request == RequestType.LOGIN) {

                        try {

                            LoginRequest loginRequest = LoginRequest.getInstance(root);

                            LoginDetails details = loginRequest.getDetails();

                            Log.v(LOG_TAG, "Client username: " + details.getUsername());

                            ClientHandler clientHandler = new ClientHandler(client, details.getUsername(), in, out);
                            Thread t = new Thread(clientHandler);
                            Log.i(LOG_TAG, String.format("Adding %s to active clients list", details.getUsername()));
                            Server.clients.add(clientHandler);
                            //TODO : Handle this to only add new users to the list.
                            t.start();

                            out.println(new Response(ResponseStatus.SUCCESS).toJSON());
                            loggedIn.set(true);

                        } catch (IllegalArgumentException | JSONException ex) {
                            Log.e(LOG_TAG, "Error parsing json", ex);
                            errorResponse.setMessage(ex.getMessage());
                            out.println(errorResponse.toJSON());
                            loggedIn.set(true);
                        }
                    } else {
                        errorResponse.setMessage("The first request should always be a login request.");
                        out.println(errorResponse.toJSON());
                        loggedIn.set(true);
                    }
                } catch (IllegalArgumentException ex) {
                    Log.e(LOG_TAG, "IllegalArgumentException occurred", ex);
                    errorResponse.setMessage("The first request should always be a login request.");
                    out.println(errorResponse.toJSON());
                    loggedIn.set(true);

                }
            }

        } catch (IOException ex){
            Log.e(LOG_TAG, "IOException occurred: "+ex.getMessage(), ex);
            loggedIn.set(true);
        }
    }
}
