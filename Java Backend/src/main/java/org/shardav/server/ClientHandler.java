package org.shardav.server;

import org.json.JSONObject;
import org.json.JSONTokener;
import shardav.utils.Log;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler implements Runnable {

    private static final String LOG_TAG = ClientHandler.class.getSimpleName();

    private String name;
    final DataInputStream in;
    final DataOutputStream out;
    Socket socket;
    boolean isLoggedIn;

    public ClientHandler(Socket s, String name, DataInputStream in, DataOutputStream out) {

        this.socket = s;
        this.name = name;
        this.in = in;
        this.out = out;
        this.isLoggedIn = true;

    }

    @Override
    public void run() {
        while (isLoggedIn) {

            try {

                String received = in.readUTF();

                JSONTokener jsonTokenizer = new JSONTokener(received);
                JSONObject object = new JSONObject(jsonTokenizer);
                object.put("from", this.name);
                String message = object.getString("message");
                String to = object.getString("to");
                long time = object.getLong("time");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss aa");
                String timestamp = dateFormat.format(new Date(time));
                Log.i(LOG_TAG, String.format("To: %s, From: %s, Message: %s, Timestamp: %s",
                        to, this.name, message, timestamp));
                if (to.equals("server") && message.equals("logout"))
                    to = this.name;
                object.put("to", to);
                for (ClientHandler client : Server.clients) {
                    if (client.name.equals(to) && client.isLoggedIn) {
                        client.out.writeUTF(object.toString());
                        if (message.equals("logout") && to.equals(this.name))
                            disconnect(false);
                        break;
                    }
                }

            } catch (IOException ex) {
                if (ex instanceof EOFException)
                    disconnect(false);
                else
                    Log.e(LOG_TAG, "Error occurred", ex);
            }

        }
    }

    String getName() {
        return name;
    }

    protected void disconnect(boolean kicked) {
        try {
            isLoggedIn = false;
            socket.close();
            in.close();
            out.close();
            Server.clients.remove(this);
            Log.i(LOG_TAG, name + (kicked ? " was kicked from the server." : " left the session."));
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Error occurred", ex);
        }
    }

}