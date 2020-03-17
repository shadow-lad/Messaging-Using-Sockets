package org.shardav.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import shardav.utils.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: PRIORITY 1:  Keep a track of the registered users in a database
// - PRIORITY 2: Save messages that are not delivered to the database
// - PRIORITY 3: Create a way to authenticate people

public class Server {

    //TODO: Implement HashMap to find clients faster.
    // static HashMap<String,Integer> clientMap;
    static List<ClientHandler> clients = new ArrayList<>(); // List of clients

    //Log tag
    private static final String LOG_TAG = Server.class.getSimpleName();

    //State of server
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);

    //Reading input from the user.
    private static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in));

    final String SETTINGS_JSON_PATH;

    //ServerSocket, should be on at all times.
    private static ServerSocket messageServerSocket;

    //Class Variables
    private int serverPort;
    private String mySQLUsername, mySQLPassword;
    private boolean verboseLogging;

    //Default constructor
    private Server() throws URISyntaxException {
        serverPort = 6969;
        mySQLUsername = "root";
        mySQLPassword = "toor";
        verboseLogging = false;

        final URI PATH = Server.class.getProtectionDomain().getCodeSource().getLocation().toURI();

        String directoryPath = Paths.get(PATH).toString();
        directoryPath = directoryPath.substring(0,directoryPath.lastIndexOf(File.separatorChar)+1);

        SETTINGS_JSON_PATH = directoryPath+"settings.json";

    }

    //Driver method to call the member functions
    public static void main(String[] args) {

        try {

            Server server = new Server();
            server.getServerConfig(); // Load the configuration from the server-config.json file
            server.loadExistingUsers(); // Load the existing users from the MySQL database
            server.startServer(); // Start the server once setup is done

        } catch (URISyntaxException | IOException ex) {

            Log.e(LOG_TAG, "An error occurred while setting up the server", ex);

        }

    }

    //Displays the help menu
    private void displayHelpMenu() {
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

    //Returns a list of currently active clients
    private List<String> getActiveClients(){
        List<String> clientList = new ArrayList<>();
        for(ClientHandler currentClient: clients){
            clientList.add(currentClient.getName());
        }
        return clientList;
    }

    //Prints the active clients
    private void printActiveClients() {
        Log.i(LOG_TAG, "Active Clients: " + (clients.size() == 0 ? "No Active Clients" : ""));
        for (String currentClient : getActiveClients()) {
            Log.i(LOG_TAG, currentClient);
        }
    }

    //Toggles verbose output
    private void toggleVerbose() {
        boolean previous = Log.getVerbose();
        if (previous)
            Log.i(LOG_TAG, "Disabling verbose output.");
        else
            Log.i(LOG_TAG, "Enabling verbose output.");
        Log.showVerbose(!previous);
    }

    //Sends the calling thread to sleep
    private void goToSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Log.e(LOG_TAG, ex.getMessage() == null ? "Thread Interrupted" : ex.getMessage(), ex);
        }
    }

    private void getServerConfig() throws IOException {

        File settingsJSON = new File(SETTINGS_JSON_PATH);

        if(settingsJSON.exists()){

            String line;
            StringBuilder json = new StringBuilder();
            //Reader to read from settings.json
            BufferedReader br = new BufferedReader(new FileReader(settingsJSON));

            while((line=br.readLine())!=null)
                json.append(line);

            Log.i(LOG_TAG, "settings.json read: "+json);

            try {

                JSONTokener tokenizer = new JSONTokener(json.toString());
                JSONObject settings = new JSONObject(tokenizer);

                Log.i(LOG_TAG, "JSON parsed: "+settings.toString(2));

                serverPort = settings.getInt("port");
                verboseLogging = settings.getBoolean("verbose-logging");
                JSONObject mySQL = settings.getJSONObject("my-sql");
                mySQLUsername = mySQL.getString("username");
                mySQLPassword = mySQL.getString("password");

                Log.showVerbose(verboseLogging);

            } catch (JSONException ex){
                Log.e(LOG_TAG, "Error reading settings.json. Falling back to default settings :"+ex.getMessage());
                writeSettingsJSON(settingsJSON);
            }


        } else {
            boolean created = settingsJSON.createNewFile();
            if(!created)
                throw new IOException("There was a problem creating settings.json. " +
                        "Make sure you have permissions to create files in the current directory.");
            Log.i(LOG_TAG, "Server started for the first time, creating settings.json...");
            writeSettingsJSON(settingsJSON);

        }

    }

    private void writeSettingsJSON(File settingsJSON)throws IOException {

        if(settingsJSON.delete())
            Log.v(LOG_TAG, "Deleted settings.json");
        if(settingsJSON.createNewFile())
            Log.v(LOG_TAG, "Created settings.json");

        JSONObject settings = new JSONObject();
        JSONObject mySQL = new JSONObject();
        mySQL.put("username",mySQLUsername);
        mySQL.put("password",mySQLPassword);
        settings.put("port",serverPort);
        settings.put("my-sql",mySQL);
        settings.put("verbose-logging",verboseLogging);

        PrintWriter pw = new PrintWriter(settingsJSON);
        pw.println(settings.toString(2));
        pw.flush();
        pw.close();

    }

    private void loadExistingUsers(){
        //TODO: Get users already registered to the server
        // NOTE: This means loading users from the MySQL database.
    }

    private void startServer() {

        try {

            Log.i(LOG_TAG, "Starting server on port 6969...");
            goToSleep(5000);

            //Creating a new server on port 6969
            messageServerSocket = new ServerSocket(6969);
            Log.i(LOG_TAG, "Server started on port 6969");
            goToSleep(2000);

            initializeServerOperations();
            startAcceptingClients();
            Log.i(LOG_TAG, "Accepting clients on port 6969");
            displayHelpMenu();

        } catch (IOException ex) {
            Log.e(LOG_TAG, "An error occurred while trying to start the server: " + ex.getMessage(), ex);
        }

    }

    //Start the thread that lets clients connect to the server
    private void startAcceptingClients() {
        new Thread(() -> {

            while (RUNNING.get()) {

                try {

                    Socket client = messageServerSocket.accept(); //Accept connections to the server

                    //TODO: Create a proper user database

                    //Creating a new thread to handle logging in
                    // so that client requests are not queued
                    Thread login = new Thread(new LoginHandler(client));
                    login.start();

                } catch (IOException ex) {
                    if(!ex.getMessage().equalsIgnoreCase("socket closed"))
                        Log.e(LOG_TAG, "An error occurred: " + ex.getMessage(), ex);
                }
            }

        }).start();
    }

    // Enable operating on the server while it is running
    private void initializeServerOperations() {
        new Thread(() -> {
            while (RUNNING.get()) {
                try {
                    String operation = STDIN.readLine();
                    if (operation.length() == 1)
                        switch (operation.charAt(0)) {
                            case 'l': // List active clients
                                printActiveClients();
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
                            default: Log.e(LOG_TAG, "Operation not recognised, enter ? for possible operations.");
                        }
                    else
                        Log.e(LOG_TAG, "Operation not recognised, enter ? for possible operations.");

                } catch (IOException ex) {
                    Log.e(LOG_TAG, "An error occurred while trying to read from System.in", ex);
                }
            }
        }).start();
    }

    //Cleans up and closes the server ;
    private void quitServer() throws IOException {

        verboseLogging = Log.getVerbose();
        Log.showVerbose(false);
        File settingsJSON = new File(SETTINGS_JSON_PATH);
        writeSettingsJSON(settingsJSON);
        Log.showVerbose(verboseLogging);

        Log.i(LOG_TAG, "Shutting down server...");
        goToSleep(2000);
        if (RUNNING.get()) {
            try {
                RUNNING.set(false);
                for (ClientHandler currentClient : clients) {
                    if (currentClient.isLoggedIn)
                        currentClient.disconnect(true);
                }
                messageServerSocket.close();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Server force closed: " + ex.getMessage(), ex);
            } finally {
                Log.i(LOG_TAG, "Server Shutdown, EXITING...");
            }
        }
    }

}