package org.shardav.server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.shardav.server.Server;
import org.shardav.server.comms.Request.RequestType;
import org.shardav.server.comms.Response;
import org.shardav.server.comms.Response.ResponseStatus;
import org.shardav.utils.Log;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private static final String LOG_TAG = Server.class.getSimpleName() + ": " + ClientHandler.class.getSimpleName();

    // Executors for Client Requests
    private final ExecutorService MESSAGE_EXECUTOR = Executors.newSingleThreadExecutor();
    private final ExecutorService LOGIN_EXECUTOR = Executors.newSingleThreadExecutor();

    // Client variables
    private String email;
    public boolean isLoggedIn;

    // Socket variables
    private final BufferedReader in;
    protected final PrintWriter out;
    private final Socket socket;

    // Operation Handlers
    private final MessageHandler messageHandler;
    private final SignUpHandler registrationHandler;

    public ClientHandler(Socket socket, String email, final BufferedReader in, final PrintWriter out) {
        this.socket = socket;
        this.email = email;
        this.in = in;
        this.out = out;
        this.isLoggedIn = false;
        this.messageHandler = new MessageHandler(ClientHandler.this);
        this.registrationHandler = new SignUpHandler(ClientHandler.this);
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

                Response errorResponse = new Response(ResponseStatus.INVALID);

                try {
                    RequestType requestType = RequestType.valueOf(requestObject.getAsJsonPrimitive("request").getAsString());

                    switch (requestType) {
                        case users:
                            //TODO: Send back the list of registered users based on search
                            break;
                        case message:
                            if (isLoggedIn) {
                                MESSAGE_EXECUTOR.submit(() -> messageHandler.handleJson(requestObject));
                            } else {
                                Log.v(LOG_TAG, socket.getInetAddress() + " sent message request without log in");
                                errorResponse.setMessage("User not logged in");
                                out.println(errorResponse.toJSON());
                                out.flush();
                            }
                            break;
                        case login:
                        case registration:
                        case verify:
                            if (!isLoggedIn) {
                                switch (requestType) {
                                    case login: //TODO: handle login
                                        break;
                                    case registration:
                                    case verify: LOGIN_EXECUTOR.submit(()-> registrationHandler.handleJson(requestObject));
                                    break;
                                }
                            } else {
                                Log.v(LOG_TAG, email + " is already logged in, invalid request");
                                errorResponse.setMessage("User is already logged in");
                                out.println(errorResponse);
                                out.flush();
                            }
                        case logout:
                            disconnect(false);
                        default:
                            Log.d(LOG_TAG, "Made a request of type: " + requestType.toString());
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
                    disconnect(true);
                }
            }
        }
    }

    public String getEmail() {
        return email;
    }

    synchronized public void disconnect(boolean kicked) {

        if (socket != null && !socket.isClosed() && socket.isConnected()) {

            try {

                isLoggedIn = false;

                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();

                Server.ACTIVE_CLIENT_MAP.remove(this.email);
                Log.i(LOG_TAG, email + (kicked ? " was kicked from the server." : " left the session."));

            } catch (IOException ex) {
                Log.e(LOG_TAG, "Error occurred", ex);
            }
        }
    }

    protected void setEmail(String email) {
        this.email = email;

    }
}