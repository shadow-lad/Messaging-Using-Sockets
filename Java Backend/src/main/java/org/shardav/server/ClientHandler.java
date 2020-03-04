package org.shardav.server;

import org.json.JSONObject;
import org.json.JSONTokener;
import shardav.utils.Log;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler implements Runnable {

    //TODO: Use the classes created in com.shardav.server.comms to make the code more readable
    private static final String LOG_TAG = ClientHandler.class.getSimpleName();

    private String name;
    final BufferedReader in;
    final BufferedWriter out;
    private Socket socket;
    boolean isLoggedIn;

    public ClientHandler(Socket s, String name, BufferedReader in, BufferedWriter out) {

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

                String received = in.readLine();

                if(received == null)
                    continue;

                JSONTokener jsonTokenizer = new JSONTokener(received);
                JSONObject object = new JSONObject(jsonTokenizer);

                object.put("from", this.name);
                String message = object.getString("message");
                String to = object.getString("to");
                long time = object.getLong("time");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
                String timestamp = dateFormat.format(new Date(time));
                Log.i(LOG_TAG, String.format("To: %s, From: %s, Message: %s, Timestamp: %s",
                        to, this.name, message, timestamp));
                if (to.equals("server") && message.equals("logout"))
                    to = this.name;
                object.put("to", to);
                if (to.equals("everybody")) {
                    sendToEverybody(object);
                } else
                    sendPrivate(to, message, object);

            } catch (IOException ex) {
                if (ex instanceof EOFException)
                    disconnect(false);
                else {
                    Log.e(LOG_TAG, "Error occurred", ex);
                    System.exit(0);
                }
            }

        }
    }

    String getName() {
        return name;
    }

    synchronized protected void disconnect(boolean kicked) {
        if(socket != null && socket.isConnected()) {
            try {
                isLoggedIn = false;

                socket.close();
                in.close();
                out.close();

                in.close();
                out.close();
                socket.close();

                Server.clients.remove(this);
                Log.i(LOG_TAG, name + (kicked ? " was kicked from the server." : " left the session."));
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Error occurred", ex);
            }
        }
    }


    private void sendPrivate(String to, String message, JSONObject object) throws IOException {

        for (ClientHandler client : Server.clients) {
            if (client.name.equals(to) && client.isLoggedIn) {
                client.out.write(object.toString()+"\n");
                if (message.equals("logout") && to.equals(this.name))
                    disconnect(false);
                break;
            }
        }

    }

    private void sendToEverybody(JSONObject object) throws IOException {
        for (ClientHandler client : Server.clients) {
            if (!client.getName().equals(name) && client.isLoggedIn)
                client.out.write(object.toString()+"\n");

        }
    }

}