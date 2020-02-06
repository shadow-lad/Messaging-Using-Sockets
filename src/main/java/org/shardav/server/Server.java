package org.shardav.server;

import shardav.utils.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: PRIORITY 1:  Keep a track of the registered users in a database
// - PRIORITY 2: Save messages that are not delivered to the database
// - PRIORITY 3: Create a way to authenticate people

public class Server {

    static List<ClientHandler> clients = new ArrayList<>();

    private static final String LOG_TAG = Server.class.getCanonicalName();
    private static final AtomicBoolean running = new AtomicBoolean(true);

    private static ServerSocket server;

    public static void main(String[] args) {

        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in,
                        Charset.availableCharsets()
                                .getOrDefault("utf-8", Charset.defaultCharset())
                )
        );

        try {

            Log.i(LOG_TAG, "Starting server on port 6969...");
            goToSleep(5000);

            server = new ServerSocket(6969);
            Log.i(LOG_TAG, "Server started on port 6969");
            goToSleep(2000);

            Thread acceptClient = new Thread(() -> {

                while (running.get()) {

                    try {

                        Socket client = server.accept();
                        DataInputStream in = new DataInputStream(client.getInputStream());
                        DataOutputStream out = new DataOutputStream(client.getOutputStream());

                        String userName = in.readUTF();

                        Log.i(LOG_TAG, "New Client Request Received: " + userName);

                        Log.v(LOG_TAG, "Creating a new Handler for " + userName);

                        ClientHandler clientHandler = new ClientHandler(client, userName, in, out);

                        Thread t = new Thread(clientHandler);

                        Log.i(LOG_TAG, "Adding " + userName + " to active clients list");
                        clients.add(clientHandler);

                        t.start();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            });

            Thread operations = new Thread(() -> {
                while (running.get()) {
                    try {
                        String operation = input.readLine();
                        if (operation.length() == 1)
                            switch (operation.charAt(0)) {
                                case 'l':
                                    printActiveClientTable();
                                    break;
                                case 'v':
                                    toggleVerbose();
                                    break;

                                case 'q':
                                    quitServer();
                                    break;

                                case '?':
                                    displayMenu();
                                    break;
                            }
                        else
                            Log.e(LOG_TAG, "Operation not recognised, enter ? for possible operations.");

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            acceptClient.start();
            Log.i(LOG_TAG, "Accepting clients on port 6969");
            operations.start();
            displayMenu();

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static void displayMenu() {
        System.out.println(
                "--------\n" +
                        "  HELP\n" +
                        "--------\n" +
                        "\n" +
                        "l - List connected clients\n" +
                        "v - Toggle verbose logging\n" +
                        "q - Shutdown server\n" +
                        "? - Show this menu\n");
    }

    private static void printActiveClientTable() {
        Log.i(LOG_TAG, "Active Clients: " + (clients.size() == 0 ? "No Active Clients" : ""));
        for (ClientHandler client1 : clients) {
            Log.i(LOG_TAG, client1.getName());
        }
    }

    private static void toggleVerbose() {
        boolean previous = Log.getVerbose();
        if (previous)
            Log.i(LOG_TAG, "Disabling verbose output.");
        else
            Log.i(LOG_TAG, "Enabling verbose output.");
        Log.showVerbose(!previous);
    }

    private static void goToSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Log.e(LOG_TAG, ex.getMessage() == null ? "Thread Interrupted" : ex.getMessage(), ex);
        }
    }

    private static void goToSleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException ex) {
            Log.e(LOG_TAG, ex.getMessage() == null ? "Thread Interrupted" : ex.getMessage(), ex);
        }
    }

    private static void quitServer() {
        Log.i(LOG_TAG, "Shutting down server...");
        goToSleep(2000);
        running.set(false);
        System.exit(0);
    }

}