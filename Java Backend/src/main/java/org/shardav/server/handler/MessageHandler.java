package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.shardav.server.Server;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.messages.GlobalMessageDetails;
import org.shardav.server.comms.messages.Message;
import org.shardav.server.comms.messages.Message.MessageType;
import org.shardav.server.comms.messages.PrivateMessageDetails;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

public class MessageHandler {

    private static final String LOG_TAG = MessageHandler.class.getSimpleName();

    private final ClientHandler client;
    private final Gson gson;

    public MessageHandler(ClientHandler client) {
        this.client = client;
        this.gson = new Gson();
    }

    protected void handleJson(JsonObject root) {
        try {
            MessageType messageType = MessageType.valueOf(root.getAsJsonPrimitive("type").getAsString());
            JsonObject messageDetails = root.getAsJsonObject("details");
            if (messageDetails == null) {
                throw new JsonSyntaxException("JSON object \"details\" not present.");
            }
            Message<?> message;
            switch (messageType) {
                case global:
                    message = new Message<>(gson.fromJson(messageDetails, GlobalMessageDetails.class).setFrom("server"));
                    sendGlobalMessage(message);
                    break;
                case personal:
                    message = new Message<>(gson.fromJson(messageDetails, PrivateMessageDetails.class).setFrom("server"));
                    sendPersonalMessage(((PrivateMessageDetails)message.getDetails()).getTo(), message);
                    break;
            }
        } catch (JsonSyntaxException ex) {
            Log.e("An error occurred while parsing the message", ex.getMessage(), ex);
            Response errorResponse = new Response(ResponseStatus.INVALID);
            errorResponse.setMessage(ex.getMessage());
            client.out.println(errorResponse.toJSON());
            client.out.flush();
        } catch (EnumConstantNotPresentException ex) {
            Response errorResponse = new Response(ResponseStatus.INVALID);
            Log.d(LOG_TAG, "Message type not identified: " + root.getAsJsonPrimitive("type").getAsString());
            errorResponse.setMessage("Invalid message type");
            client.out.println(errorResponse.toJSON());
            client.out.flush();
        }
    }

    private void sendPersonalMessage(String recipient, Message<?> message) {
        ClientHandler recipientClient = Server.ACTIVE_CLIENT_MAP.get(recipient);
        String messageJSON = gson.toJson(message);
        if (recipientClient.isLoggedIn) {
            recipientClient.out.println(messageJSON);
            recipientClient.out.flush();
        } else {
            try {
                Database database = Database.getInstance();
                ServerExecutors.getDatabaseExecutor().submit(() -> database.addMessage(message.getDetails()));
            } catch (InstantiationException ex) {
                Log.e(LOG_TAG, "An error occurred while trying to fetch an instance of database", ex);
            }
        }
        System.out.println(messageJSON);
    }

    private void sendGlobalMessage(Message<?> message) {
        String messageJSON = gson.toJson(message);
        for (ClientHandler client : Server.ACTIVE_CLIENT_MAP.values()) {
            if (!client.getEmail().equals(this.client.getEmail()) && client.isLoggedIn) {
                client.out.println(messageJSON);
                client.out.flush();
            }
        }
    }
}
