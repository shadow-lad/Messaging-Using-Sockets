package org.shardav.server;

import shardav.utils.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: PRIORITY 1:  Keep a track of the registered users in a database
// - PRIORITY 2: Save messages that are not delivered to the database
// - PRIORITY 3: Create a way to authenticate people

public class Server {

    static List<ClientHandler> clients = new ArrayList<>(); // List of clients

    //TODO: Implement HashMap to find clients faster.
    // static HashMap<String,Integer> clientMap;

    //Log tag
    private static final String LOG_TAG = Server.class.getSimpleName();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    private static ServerSocket server;

    //Driver method to call the member functions
    public static void main(String[] args) {
        startServer();
    }

    //Displays the help menu
    private static void displayHelpMenu() {
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

    //Prints the active client table
    private static void printActiveClientTable() {
        Log.i(LOG_TAG, "Active Clients: " + (clients.size() == 0 ? "No Active Clients" : ""));
        for (ClientHandler currentClient : clients) {
            Log.i(LOG_TAG, currentClient.getName());
        }
    }

    //Toggles verbose output
    private static void toggleVerbose() {
        boolean previous = Log.getVerbose();
        if (previous)
            Log.i(LOG_TAG, "Disabling verbose output.");
        else
            Log.i(LOG_TAG, "Enabling verbose output.");
        Log.showVerbose(!previous);
    }

    //Sends the calling thread to sleep
    private static void goToSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Log.e(LOG_TAG, ex.getMessage() == null ? "Thread Interrupted" : ex.getMessage(), ex);
        }
    }

    //Cleans up and closes the server
    private static void quitServer() {
        Log.i(LOG_TAG, "Shutting down server...");
        goToSleep(2000);
        running.compareAndSet(true,false);
        try {
            for (ClientHandler currentClient : clients) {
                if(currentClient.isLoggedIn) {
                    currentClient.isLoggedIn = false;
                    currentClient.in.close();
                    currentClient.out.close();
                    currentClient.socket.close();
                }
            }
        } catch (IOException ex){
            Log.e(LOG_TAG, "An error occurred",ex);
        }
        System.exit(0);
    }

    //TODO: Implement this
    /*private static void getServerConfig(){
        //TODO: Get server config from JSON file
        // NOTE: Use System.get("user.dir") to get current directory
    }*/

    private static void startServer(){

        try {

            Log.i(LOG_TAG, "Starting server on port 6969...");
            goToSleep(5000);

            //Creating a new server on port 6969
            server = new ServerSocket(6969);
            Log.i(LOG_TAG, "Server started on port 6969");
            goToSleep(2000);

            initializeServerOperations();
            startAcceptingClients();
            Log.i(LOG_TAG, "Accepting clients on port 6969");
            displayHelpMenu();

        } catch (IOException ex) {
            Log.e(LOG_TAG, "An error occurred while trying to start the server: "+ ex.getMessage(),ex);
        }

    }

    //Start the thread that lets clients connect to the server
    private static void startAcceptingClients(){
        new Thread(() -> {

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

        }).start();
    }

    // Enable operating on the server while it is running
    private static void initializeServerOperations(){
        new Thread(() -> {
            while (running.get()) {
                try {
                    String operation = input.readLine();
                    if (operation.length() == 1)
                        switch (operation.charAt(0)) {
                            case 'l': // List active clients
                                printActiveClientTable();
                                break;
                            case 'v': // Activate verbose logging
                                toggleVerbose();
                                break;

                            case 'q': // Shut down server
                                quitServer();
                                break;

                            case '?': // Display help menu
                                displayHelpMenu();
                                break;
                        }
                    else
                        Log.e(LOG_TAG, "Operation not recognised, enter ? for possible operations.");

                } catch (IOException ex) {
                    Log.e(LOG_TAG, "An error occurred while trying to read from System.in", ex);
                }
            }
        }).start();
    }

}