package org.shardav.server;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.messages.GlobalMessageDetails;
import org.shardav.server.comms.messages.Message;
import org.shardav.server.comms.messages.PrivateMessageDetails;
import shardav.utils.Log;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    //TODO: Use the classes created in com.shardav.server.comms to make the code more readable
    private static final String LOG_TAG = Server.class.getSimpleName() + ": " + ClientHandler.class.getSimpleName();

    private String email;
    final BufferedReader in;
    final PrintWriter out;
    private final Socket socket;
    boolean isLoggedIn;

    public ClientHandler(Socket socket, String email, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.email = email;
        this.in = in;
        this.out = out;
        this.isLoggedIn = true;
    }

    @Override
    public void run() {
        while (isLoggedIn) {
            try {
                String requestData = in.readLine();

                if (requestData == null)
                    continue;

                JSONTokener jsonTokenizer = new JSONTokener(requestData);
                JSONObject requestObject = new JSONObject(jsonTokenizer);

                Response errorResponse = new Response(ResponseStatus.INVALID);

                try {
                    RequestType requestType = RequestType.getRequestType(requestObject.getString("request"));

                    if (requestType == RequestType.MESSAGE) {
                        Message message = Message.getInstance(requestObject);

                        switch (message.getMessageType()) {
                            case GLOBAL:
                                GlobalMessageDetails globalMessageDetails = (GlobalMessageDetails) message.getDetails();
                                globalMessageDetails.setSender(this.email);
                                JSONObject globalMessageObject = new JSONObject(globalMessageDetails.toMap());
                                sendGlobalMessage(globalMessageObject);
                                break;

                            case PRIVATE:
                                PrivateMessageDetails privateMessageDetails = (PrivateMessageDetails) message.getDetails();
                                privateMessageDetails.setSender(this.email);
                                String recipient = privateMessageDetails.getRecipient();
                                JSONObject privateMessageObject = new JSONObject(privateMessageDetails.toMap());
                                sendPrivate(recipient, privateMessageObject);
                                break;
                        }
                    } else if (requestType == RequestType.LOGOUT)
                        disconnect(false);
                } catch (IllegalArgumentException ex) {
                    Log.e(LOG_TAG, "Error occurred", ex);
                    errorResponse.setMessage("Request type not recognised by server.");
                    out.println(errorResponse.toJSON());
                }
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

    String getEmail() {
        return email;
    }

    synchronized protected void disconnect(boolean kicked) {
        if (socket != null && socket.isConnected()) {
            try {
                isLoggedIn = false;

                socket.close();
                in.close();
                out.close();

                in.close();
                out.close();
                socket.close();

                Server.activeClients.remove(this);
                Log.i(LOG_TAG, email + (kicked ? " was kicked from the server." : " left the session."));
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Error occurred", ex);
            }
        }
    }

    private void sendPrivate(String recipient, JSONObject object) {
        for (ClientHandler client : Server.activeClients) {
            if (client.email.equals(recipient) && client.isLoggedIn) {
                client.out.println(object.toString());
                break;
            }
        }
    }

    private void sendGlobalMessage(JSONObject object) {
        for (ClientHandler client : Server.activeClients) {
            if (!client.getEmail().equals(email) && client.isLoggedIn)
                client.out.println(object.toString());

        }
    }

}