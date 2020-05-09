package org.shardav.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.shardav.server.Server;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private static final String LOG_TAG = Server.class.getSimpleName() + ": " + ClientHandler.class.getSimpleName();

    // Executors for Client Requests
    protected final ExecutorService MESSAGE_EXECUTOR;
    private final ExecutorService LOGIN_EXECUTOR;

    // Client variables
    private String email;
    public boolean isLoggedIn;

    // Socket variables
    private final BufferedReader in;
    protected final PrintWriter out;
    private final Socket socket;

    // Operation Handlers
    private final MessageHandler messageHandler;
    private final RegistrationHandler registrationHandler;
    private final LoginHandler loginHandler;

    private final Gson gson;

    public ClientHandler(Socket socket, Database database, GMailService mailService) throws IOException {
        this.email = null;
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.isLoggedIn = false;

        this.messageHandler = new MessageHandler(ClientHandler.this, database);
        this.registrationHandler = new RegistrationHandler(ClientHandler.this, database, mailService);
        this.loginHandler = new LoginHandler(ClientHandler.this, database);

        this.gson = new Gson();

        // Initialising executors
        this.MESSAGE_EXECUTOR = Executors.newSingleThreadExecutor();
        this.LOGIN_EXECUTOR = Executors.newSingleThreadExecutor();
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
                Response<Void> errorResponse = new Response<>(ResponseStatus.invalid, ResponseType.general);

                try {
                    RequestType requestType =
                            RequestType.valueOf(requestObject.getAsJsonPrimitive("request").getAsString());

                    switch (requestType) {
                        case users:
                            if (isLoggedIn) {
                                Response<Set<UserDetails>> userListResponse = new Response<>(ResponseStatus.success, ResponseType.user);
                                this.out.println(gson.toJson(userListResponse));
                            } else {
                                Log.v(LOG_TAG, socket.getInetAddress() + " sent message request without log in");
                                errorResponse.setMessage("User not logged in");
                                this.out.println(gson.toJson(errorResponse));
                            }
                            this.out.flush();
                            break;
                        case message:
                            if (isLoggedIn) {
                                MESSAGE_EXECUTOR.submit(() -> messageHandler.handleJson(requestObject));
                            } else {
                                Log.v(LOG_TAG, socket.getInetAddress() + " sent message request without log in");
                                errorResponse.setMessage("User not logged in");
                                out.println(gson.toJson(errorResponse));
                                out.flush();
                            }
                            break;
                        case login:
                        case registration:
                        case verify:
                            LOGIN_EXECUTOR.submit(() -> {
                                if (!isLoggedIn) {
                                    switch (requestType) {
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
                                    this.out.println(errorResponse);
                                    this.out.flush();
                                }
                            });
                            break;
                        case logout:
                            logout(false);
                            break;
                        default:
                            Log.d(LOG_TAG, "Made a request of type: " + requestType.toString());
                            errorResponse.setMessage("Invalid request");
                            this.out.println();
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

    synchronized private void logout(boolean disconnect) {
        if (isLoggedIn) {
            Log.i(LOG_TAG, this.email + " logged out.");
            Server.CLIENT_MAP.put(this.email, null);
            Server.ACTIVE_CLIENTS.remove(this.email);
            if (!disconnect) {
                this.out.println(new Response<Void>(ResponseStatus.success, ResponseType.logout));
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

                Log.i(LOG_TAG, socket.getInetAddress() + (kicked ? " was kicked from the server." : " left the session."));

            } catch (IOException ex) {
                Log.e(LOG_TAG, "Error occurred", ex);
            }
        }
    }

    protected void setEmail(String email) {
        this.email = email;

    }

    protected void setIsLoggedIn() {
        this.isLoggedIn = true;
    }
}