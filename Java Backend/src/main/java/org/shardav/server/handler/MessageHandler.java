package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.shardav.server.Server;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.messages.GlobalMessageDetails;
import org.shardav.server.comms.messages.Message;
import org.shardav.server.comms.messages.Message.MessageType;
import org.shardav.server.comms.messages.PersonalMessageDetails;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;

public class MessageHandler {

    private static final String LOG_TAG = MessageHandler.class.getSimpleName();

    private final ClientHandler client;
    private final Database database;
    private final Gson gson;

    protected MessageHandler(ClientHandler client, Database database) {
        this.client = client;
        this.database = database;
        this.gson = new Gson();
    }

    protected void handleJson(JsonObject root) {
        try {
            MessageType messageType = MessageType.valueOf(root.getAsJsonPrimitive("type").getAsString());
            JsonObject messageDetails = root.getAsJsonObject("details");
            if (messageDetails == null) {
                throw new JsonSyntaxException("JSON object \"details\" not present.");
            }
            String id = client.getEmail() + new Date().getTime();
            switch (messageType) {
                case global:
                    Message<GlobalMessageDetails> globalMessage = new Message<>(gson.fromJson(messageDetails, GlobalMessageDetails.class).setFrom(client.getEmail()).setId(id));
                    sendGlobalMessage(globalMessage);
                    break;
                case personal:
                    Message<PersonalMessageDetails> personalMessage = new Message<>(gson.fromJson(messageDetails, PersonalMessageDetails.class).setFrom(client.getEmail()).setId(id));
                    sendPersonalMessage((personalMessage.getDetails()).getTo(), personalMessage);
                    break;
            }
        } catch (JsonSyntaxException ex) {
            Log.e("An error occurred while parsing the message", ex.getMessage(), ex);
            Response<Void> errorResponse = new Response<>(ResponseStatus.invalid, ResponseType.message);
            errorResponse.setMessage(ex.getMessage());
            client.out.println(gson.toJson(errorResponse));
            client.out.flush();
        } catch (EnumConstantNotPresentException ex) {
            Response<Void> errorResponse = new Response<>(ResponseStatus.invalid, ResponseType.message);
            Log.d(LOG_TAG, "Message type not identified: " + root.getAsJsonPrimitive("type").getAsString());
            errorResponse.setMessage("Invalid message type");
            client.out.println(gson.toJson(errorResponse));
            client.out.flush();
        }
    }

    private void sendPersonalMessage(String recipient, Message<PersonalMessageDetails> message) {
        ClientHandler recipientClient = Server.CLIENT_MAP.get(recipient);
        if (!client.getFriends().contains(recipient)) {
            client.addFriends(Collections.singleton(recipient));
            ServerExecutors.getDatabaseExecutor().submit(() -> database.addUserFriends(client.getEmail(), recipient));
        }
        if (recipientClient != null) {
            String messageJSON = gson.toJson(message);
            if (recipientClient.isLoggedIn) {
                recipientClient.out.println(messageJSON);
                recipientClient.out.flush();
            } else {
                ServerExecutors.getDatabaseExecutor().submit(() -> {
                    try {
                        database.addMessage(message.getDetails());
                    } catch (SQLException ignore) {
                    }
                });
            }
        } else {
            ServerExecutors.getDatabaseExecutor().submit(() -> {
                try {
                    database.addMessage(message.getDetails());
                } catch (SQLException ex) {
                    Log.v(LOG_TAG, "Error occurred while trying to add message to database : " + ex.getLocalizedMessage(), ex);
                    Response<Void> errorResponse = new Response<>(ResponseStatus.failed, ResponseType.message);
                    errorResponse.setMessage(ex.getLocalizedMessage());
                    client.out.println(gson.toJson(errorResponse));
                    client.out.flush();
                }
            });
        }
    }

    private void sendGlobalMessage(Message<GlobalMessageDetails> message) {
        String messageJSON = gson.toJson(message);
        for (ClientHandler client : Server.CLIENT_MAP.values()) {
            if (!client.getEmail().equals(this.client.getEmail()) && client.isLoggedIn) {
                client.out.println(messageJSON);
                client.out.flush();
            }
        }
    }
}
