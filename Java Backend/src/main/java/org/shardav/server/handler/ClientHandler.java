package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.shardav.server.Server;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseEvent;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private static final String LOG_TAG = Server.class.getSimpleName() + ": " + ClientHandler.class.getSimpleName();

    // Executors for Client Requests
    protected final ExecutorService MESSAGE_EXECUTOR;
    protected final PrintWriter out;
    private final ExecutorService LOGIN_EXECUTOR;
    private final ExecutorService VOICE_EXECUTOR;
    // Socket variables
    private final Socket socket;
    private final BufferedReader in;
    // Operation Handlers
    private final MessageHandler messageHandler;
    private final RegistrationHandler registrationHandler;
    private final LoginHandler loginHandler;
    private final VoiceCallHandler voiceCallHandler;

    private final Gson gson;
    public boolean isLoggedIn;

    // Client variables
    private String email;
    private Set<String> friends;

    public ClientHandler(Socket socket, Database database, GMailService mailService) throws IOException {
        this.email = null;
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.isLoggedIn = false;

        this.messageHandler = new MessageHandler(ClientHandler.this, database);
        this.registrationHandler = new RegistrationHandler(ClientHandler.this, database, mailService);
        this.loginHandler = new LoginHandler(ClientHandler.this, database);
        this.voiceCallHandler = new VoiceCallHandler(ClientHandler.this);

        this.gson = new Gson();

        // Initialising executors
        this.MESSAGE_EXECUTOR = Executors.newSingleThreadExecutor();
        this.LOGIN_EXECUTOR = Executors.newSingleThreadExecutor();
        this.VOICE_EXECUTOR = Executors.newSingleThreadExecutor();
    }

    public boolean isOnVoiceCall() {
        return voiceCallHandler.isOnVoiceCall();
    }

    public void startTimeOut() {
        this.voiceCallHandler.startTimeOut();
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                String requestData = in.readLine();

                if (requestData == null) {
                    disconnect(false);
                    continue;
                }

                JsonObject requestObject = JsonParser.parseString(requestData).getAsJsonObject();
                Response<Void> errorResponse = new Response<>(ResponseEvent.invalid, ResponseType.general);

                try {
                    RequestType type =
                            RequestType.valueOf(requestObject.getAsJsonPrimitive("type").getAsString());

                    switch (type) {
                        case users:
                            if (isLoggedIn) {
                                Response<Set<UserDetails>> userListResponse = new Response<>(ResponseEvent.success, ResponseType.users);
                                userListResponse.setDetails(new HashSet<>(Server.CLIENT_DETAILS_MAP.values()));
                                this.out.println(gson.toJson(userListResponse));
                            } else {
                                Log.v(LOG_TAG, socket.getInetAddress().getHostAddress() + " sent message request without log in");
                                errorResponse.setMessage("User not logged in");
                                this.out.println(gson.toJson(errorResponse));
                            }
                            this.out.flush();
                            break;
                        case message:
                            if (isLoggedIn) {
                                MESSAGE_EXECUTOR.submit(() -> messageHandler.handleJson(requestObject));
                            } else {
                                Log.v(LOG_TAG, socket.getInetAddress().getHostAddress() + " sent message request without log in");
                                errorResponse.setMessage("User not logged in");
                                this.out.println(gson.toJson(errorResponse));
                                this.out.flush();
                            }
                            break;
                        case login:
                        case registration:
                        case verify:
                            LOGIN_EXECUTOR.submit(() -> {
                                if (!isLoggedIn) {
                                    switch (type) {
                                        case login:
                                            loginHandler.handleJson(requestObject);
                                            break;
                                        case registration:
                                        case verify:
                                            registrationHandler.handleJson(requestObject);
                                            break;
                                    }
                                } else {
                                    Log.v(LOG_TAG, email + " is already logged in, invalid request");
                                    errorResponse.setMessage("User is already logged in");
                                    this.out.println(gson.toJson(errorResponse));
                                    this.out.flush();
                                }
                            });
                            break;
                        case logout:
                            logout(false);
                            break;
                        case voice:
                            if (this.isLoggedIn) {
                                VOICE_EXECUTOR.submit(()-> voiceCallHandler.handleJson(requestObject));
                            } else {
                                Log.v(LOG_TAG, socket.getInetAddress().getHostAddress() + " made voice call request without log in");
                                errorResponse.setMessage("User not logged in");
                                this.out.println(gson.toJson(errorResponse));
                                this.out.flush();
                            }
                            break;
                        default:
                            Log.d(LOG_TAG, "Made a request of type: " + type.toString());
                            errorResponse.setMessage("Invalid request");
                            this.out.println(gson.toJson(errorResponse));
                            this.out.flush();
                    }

                } catch (EnumConstantNotPresentException ex) {
                    Log.e(LOG_TAG, "Error occurred", ex);
                    errorResponse.setMessage("Request type not recognised by server.");
                    this.out.println(gson.toJson(errorResponse));
                    this.out.flush();
                }
            } catch (IOException ex) {
                if (ex instanceof SocketException)
                    disconnect(false);
                else {
                    Log.e(LOG_TAG, "Error occurred", ex);
                    disconnect(true);
                }
            }
        }
    }

    public String getEmail() {
        return email;
    }

    protected void setEmail(String email) {
        this.email = email;

    }

    synchronized private void logout(boolean disconnect) {
        if (isLoggedIn) {
            Log.i(LOG_TAG, this.email + " logged out.");
            Server.CLIENT_MAP.put(this.email, null);
            Server.ACTIVE_CLIENTS.remove(this.email);
            if (!disconnect) {
                this.out.println(gson.toJson(new Response<Void>(ResponseEvent.success, ResponseType.logout)));
                this.out.flush();
            }
            this.isLoggedIn = false;
            this.email = null;
        }
    }

    synchronized public void disconnect(boolean kicked) {

        if (socket != null && !socket.isClosed() && socket.isConnected()) {

            try {

                logout(true);

                socket.shutdownInput();
                socket.shutdownOutput();
                socket.setKeepAlive(false);
                socket.close();

                Log.i(LOG_TAG, socket.getInetAddress().getHostAddress() + (kicked ? " was kicked from the server." : " left the session."));

            } catch (IOException ex) {
                Log.e(LOG_TAG, "Error occurred", ex);
            }
        }
    }

    protected void setIsLoggedIn() {
        this.isLoggedIn = true;
        Log.i(LOG_TAG, this.email + " logged in.");
    }

    protected void addFriends(Collection<String> friends) {
        if (this.friends == null) {
            this.friends = new HashSet<>(friends);
        } else {
            this.friends.addAll(friends);
        }
    }

    protected String getIpAddress() {
        return this.socket.getInetAddress().getHostAddress();
    }

    protected int getPort() {
        return this.socket.getPort();
    }

    protected Set<String> getFriends() {
        return this.friends;
    }
}