package org.shardav.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.login.LoginDetails;
import org.shardav.server.comms.login.LoginRequest;
import shardav.utils.Log;

import java.io.*;
import java.net.Socket;

public class LoginHandler implements Runnable {

    private Socket client;
    private static final String LOG_TAG = LoginHandler.class.getSimpleName();

    LoginHandler(Socket client){
        this.client = client;
    }

    @Override
    public void run() {

        try{

            Response errorResponse = new Response(ResponseStatus.INVALID);

            BufferedReader in = new BufferedReader(new InputStreamReader(new DataInputStream(client.getInputStream())));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(client.getOutputStream())));

            Log.i(LOG_TAG, "New login request received from : " + client.getInetAddress());

            String json = in.readLine();

            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject root = new JSONObject(jsonParser);

            try {

                LoginRequest loginRequest = LoginRequest.getInstance(root);

                LoginDetails details = loginRequest.getDetails();

                Log.v(LOG_TAG, "Client username: "+details.getUsername());

                ClientHandler clientHandler = new ClientHandler(client,details.getUsername(),in, out);
                Thread t = new Thread(clientHandler);
                Log.i(LOG_TAG, String.format("Adding %s to active clients list",details.getUsername()));
                Server.clients.add(clientHandler);
                //TODO : Handle this to only add clients if they aren't already registered
                t.start();

                out.write(new Response(ResponseStatus.SUCCESS).toJSON());

            } catch (IllegalArgumentException | JSONException ex){
                Log.e(LOG_TAG,"Error parsing json",ex);
                errorResponse.setMessage(ex.getMessage());
                out.write(errorResponse.toJSON()+"\n");
            }

        } catch (IOException ex){
            Log.e(LOG_TAG, "IOException occurred: "+ex.getMessage(), ex);
        }
    }
}
