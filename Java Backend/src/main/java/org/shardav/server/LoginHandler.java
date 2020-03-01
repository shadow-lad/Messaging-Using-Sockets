package org.shardav.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import shardav.utils.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LoginHandler implements Runnable {

    //TODO: Use the classes created in org.shardav.server.comms to handle request and make the code more readable
    private Socket client;
    private static final String LOG_TAG = LoginHandler.class.getSimpleName();

    LoginHandler(Socket client){
        this.client = client;
    }

    @Override
    public void run() {

        try{
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            Log.i(LOG_TAG, "New login request received from : " + client.getInetAddress());

            String json = in.readUTF();

            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject root = new JSONObject(jsonParser);

            String messageType = root.getString("type");

            JSONObject response = new JSONObject();

            if(messageType.equals("login")) {

                //Log.v(LOG_TAG, "Creating a new Handler for " + userName);

                try {

                    JSONObject message = root.getJSONObject("message");
                    String userName = message.getString("user-name");

                    Log.v(LOG_TAG, "Client name: "+userName);

                    ClientHandler clientHandler = new ClientHandler(client, userName, in, out);
                    Thread t = new Thread(clientHandler);
                    Log.i(LOG_TAG, "Adding " + userName + " to active clients list");
                    Server.clients.add(clientHandler); //Adding the client to the list of clients
                    //TODO: Handle this to only add clients if there aren't already registered
                    t.start();

                } catch (JSONException ex){
                    response.put("status","invalid");
                    response.put("message",ex.getCause());
                    Log.e(LOG_TAG, "Invalid JSON", ex);
                }
            } else {
                response.put("status","invalid");
                response.put("message","First request should be to login with username.");
            }
            out.writeUTF(response.toString());

        } catch (IOException ex){
            Log.e(LOG_TAG, "IOException occurred: "+ex.getMessage(), ex);
        }
    }
}
