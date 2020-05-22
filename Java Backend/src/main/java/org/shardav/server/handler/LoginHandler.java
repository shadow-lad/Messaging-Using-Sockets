package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.shardav.server.Server;
import org.shardav.server.ServerExecutors;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.comms.messages.Message;
import org.shardav.server.comms.messages.PersonalMessageDetails;
import org.shardav.server.sql.Database;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class LoginHandler {

    private final ClientHandler client;
    private final Gson gson;

    private final Database database;

    protected LoginHandler(ClientHandler client, Database database) {
        this.client = client;
        this.gson = new Gson();
        this.database = database;
    }

    public void handleJson(JsonObject root) {
        Response<Void> errorResponse = new Response<>(ResponseStatus.failed, ResponseType.login);
        if (root.has("details")) {
            UserDetails loginDetails = gson.fromJson(root.getAsJsonObject("details"), UserDetails.class);
            if (loginDetails.hasPassword()) {
                if (loginDetails.hasEmail()) {
                    loginUsingEmail(loginDetails);
                } else if (loginDetails.hasUsername()) {
                    loginUsingUsername(loginDetails);
                } else {
                    errorResponse.setMessage("Neither username nor email is present");
                    client.out.println(gson.toJson(errorResponse));
                    client.out.flush();
                }
            } else {
                errorResponse.setMessage("Password not provided");
                client.out.println(gson.toJson(errorResponse));
                client.out.flush();
            }
        } else {
            errorResponse.setMessage("Json object details not present");
            client.out.println(gson.toJson(errorResponse));
            client.out.flush();
        }
    }

    private void loginUsingEmail(UserDetails loginDetails) {
        ServerExecutors.getDatabaseResultExecutor().submit(()->{
            try {
                UserDetails userDetails = database.fetchUserDetailsByMail(loginDetails.getEmail());
                if (userDetails != null) {
                    if (userDetails.getPassword().equals(loginDetails.getPassword())) {
                        userDetails.setPassword(null);
                        List<String> friends = database.getFriends(userDetails.getEmail());
                        sendLoginSuccessful(userDetails, friends);
                    } else {
                        Response<Void> loginFailed = new Response<>(ResponseStatus.failed, ResponseType.login, "Wrong credentials");
                        client.out.println(gson.toJson(loginFailed));
                        client.out.flush();
                    }
                } else {
                    Response<Void> userNotFound = new Response<>(ResponseStatus.failed,
                            ResponseType.login, "User not found");
                    client.out.println(gson.toJson(userNotFound));
                    client.out.flush();
                }
            } catch (SQLException ex) {
                Response<Void> errorResponse = new Response<>(ResponseStatus.failed,
                        ResponseType.login,
                        "An error occurred while trying to log the user in.");
                client.out.println(gson.toJson(errorResponse));
                client.out.flush();
            }
        });
    }

    private void sendLoginSuccessful(UserDetails userDetails, List<String> friends) {
        Response<UserDetails> success = new Response<>(ResponseStatus.success, ResponseType.login, userDetails);
        List<UserDetails> friendsDetails = Server.CLIENT_DETAILS_MAP.values()
                .stream()
                .filter(userDetails1 -> friends.contains(userDetails1.getEmail()))
                .collect(Collectors.toList());
        userDetails.setFriends(friendsDetails);
        client.addFriends(friends);
        client.out.println(gson.toJson(success));
        client.out.flush();
        setAsLoggedIn(userDetails.getEmail());
    }

    private void loginUsingUsername(UserDetails loginDetails) {
        ServerExecutors.getDatabaseResultExecutor().submit(()->{
            try {
                UserDetails userDetails = database.fetchUserDetailsByUsername(loginDetails.getUsername());
                if (userDetails != null) {
                    if (userDetails.getPassword().equals(loginDetails.getPassword())) {
                        userDetails.setPassword(null);
                        List<String> friends = database.getFriends(userDetails.getEmail());
                        sendLoginSuccessful(userDetails, friends);
                    } else {
                        Response<Void> loginFailed = new Response<>(ResponseStatus.failed,
                                ResponseType.login, "Wrong credentials");
                        client.out.println(gson.toJson(loginFailed));
                        client.out.flush();
                    }
                } else {
                    Response<Void> userNotFound = new Response<>(ResponseStatus.failed,
                            ResponseType.login, "User not found");
                    client.out.println(gson.toJson(userNotFound));
                    client.out.flush();
                }

            } catch (SQLException ex) {
                Response<Void> errorResponse = new Response<>(ResponseStatus.failed,
                        ResponseType.login,
                        "An error occurred while trying to log the user in.");
                client.out.println(gson.toJson(errorResponse));
                client.out.flush();
            }
        });
    }

    private void setAsLoggedIn(String email) {
        client.setEmail(email);
        client.setIsLoggedIn();
        Server.ACTIVE_CLIENTS.add(email);
        Server.CLIENT_MAP.put(email, client);
        postLogin();
    }

    private void postLogin() {
        ServerExecutors.getDatabaseResultExecutor().submit(()->{
            try {
                List<Message<PersonalMessageDetails>> messageList = database.fetchMessagesByEmail(client.getEmail());
                messageList.forEach(message -> {
                    client.out.println(gson.toJson(message));
                    client.out.flush();
                });
            } catch (SQLException ignore) {
            }
        });
    }

}
