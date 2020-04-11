package org.shardav.server.handler;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.Server;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.messages.GlobalMessageDetails;
import org.shardav.server.comms.messages.Message;
import org.shardav.server.comms.messages.PrivateMessageDetails;
import org.shardav.utils.Log;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private static final String LOG_TAG = Server.class.getSimpleName() + ": " + ClientHandler.class.getSimpleName();

    private String email;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Socket socket;
    public boolean isLoggedIn;

    public ClientHandler(Socket socket, String email, final BufferedReader in, final PrintWriter out) {
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
                    } else if (requestType == RequestType.LOGOUT) {
                        disconnect(false);
                    } else {
                        Log.d(LOG_TAG, "Made a request of type: " + requestType.getValue());
                        errorResponse.setMessage("Invalid request");
                        out.println(errorResponse.toJSON());
                        out.flush();
                    }
                } catch (IllegalArgumentException ex) {
                    Log.e(LOG_TAG, "Error occurred", ex);
                    errorResponse.setMessage("Request type not recognised by server.");
                    out.println(errorResponse.toJSON());
                    out.flush();
                }
            } catch (IOException ex) {
                if (ex instanceof SocketException)
                    disconnect(false);
                else {
                    Log.e(LOG_TAG, "Error occurred", ex);

                    System.exit(0);
                }
            }
        }
    }

    public String getEmail() {
        return email;
    }

    synchronized public void disconnect(boolean kicked) {

        if (socket != null && socket.isConnected()) {
            try {
                isLoggedIn = false;

                socket.close();
                in.close();
                out.close();

                in.close();
                out.close();
                socket.close();

                Server.activeClientMap.remove(this.email);
                Log.i(LOG_TAG, email + (kicked ? " was kicked from the server." : " left the session."));
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Error occurred", ex);
            }
        }

    }

    private void sendPrivate(String recipient, JSONObject object) {
        ClientHandler recipientClient = Server.activeClientMap.get(recipient);
        if (recipientClient.isLoggedIn) {
            recipientClient.out.println(object.toString());
            recipientClient.out.flush();
        }
    }

    private void sendGlobalMessage(JSONObject object) {
        for (ClientHandler client : Server.activeClientMap.values()) {
            if (!client.getEmail().equals(email) && client.isLoggedIn) {
                client.out.println(object.toString());
                client.out.flush();
            }
        }
    }

}