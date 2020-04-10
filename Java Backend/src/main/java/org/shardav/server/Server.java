package org.shardav.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.handler.ClientHandler;
import org.shardav.server.handler.VerificationHandler;
import org.shardav.server.mail.GMailService;
import org.shardav.server.sql.Database;
import org.shardav.utils.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: PRIORITY 1: Save messages that are not delivered to the database

public class Server {

    public static Map<String, ClientHandler> activeClientMap = new HashMap<>(); // Map of active clients

    public static List<UserDetails> clients = new ArrayList<>();//List of all registered users.

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

    private final Database database;

    private final GMailService gMailService;

    //Default constructor
    private Server() throws URISyntaxException, IOException, SQLException, InstantiationException {

        serverPort = 6969;
        mySQLUsername = "root";
        mySQLPassword = "toor";
        verboseLogging = false;

        try {
            this.gMailService = GMailService.getInstance();
        } catch (GeneralSecurityException | IOException ex) {
            throw new RuntimeException("Error getting an instance of GMail Service", ex);
        }

        final URI PATH = Server.class.getProtectionDomain().getCodeSource().getLocation().toURI();

        String directoryPath = Paths.get(PATH).toString();
        directoryPath = directoryPath.substring(0,directoryPath.lastIndexOf(File.separatorChar)+1);

        SETTINGS_JSON_PATH = directoryPath+"settings.json";

        getServerConfig(); // Load the configuration from the settings.json file
        database = Database.getInstance(mySQLUsername, mySQLPassword);

    }

    //Driver method to call the member functions
    public static void main(String[] args) {

        try {

            Server server = new Server();
            server.loadExistingUsers(); // Load the existing users from the MySQL database
            server.startServer(); // Start the server once setup is done
            Log.i(LOG_TAG, "A detailed output is available in the server.log file");

        } catch (URISyntaxException | IOException | SQLException | InstantiationException ex) {

            Log.e(LOG_TAG, "An error occurred while setting up the server : " + ex.getMessage(), ex);

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
        for(ClientHandler currentClient: activeClientMap.values())
            clientList.add(currentClient.getEmail());
        return clientList;

    }

    //Prints the active clients
    private void printActiveClients() {

        Log.i(LOG_TAG, "Active Clients: " + (activeClientMap.values().size() == 0 ? "No Active Clients" : ""));
        for (String currentClient : getActiveClients()) {
            Log.i(LOG_TAG, currentClient);
        }

    }

    //Toggles verbose output
    private void toggleVerbose() {

        boolean previous = Log.verboseIsShown();
        Log.i(LOG_TAG, previous ? "Disabling verbose output." : "Enabling verbose output.");
        Log.showVerbose(!previous);
        Log.d(LOG_TAG, clients.toString());

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

        Log.i(LOG_TAG, "Loading settings if exists...");

        if (settingsJSON.exists()) {

            String line;
            StringBuilder json = new StringBuilder();
            //Reader to read from settings.json
            BufferedReader br = new BufferedReader(new FileReader(settingsJSON));

            while((line=br.readLine())!=null)
                json.append(line.trim());

            try {

                JSONTokener tokenizer = new JSONTokener(json.toString());
                JSONObject settings = new JSONObject(tokenizer);

                Log.d(LOG_TAG, "JSON parsed: "+settings.toString(2));

                serverPort = settings.getInt("port");
                verboseLogging = settings.getBoolean("verbose-logging");
                JSONObject mySQL = settings.getJSONObject("my-sql");
                mySQLUsername = mySQL.getString("username");
                mySQLPassword = mySQL.getString("password");

                Log.showVerbose(verboseLogging);

                Log.i(LOG_TAG, "Settings loaded");

            } catch (JSONException ex){
                Log.e(LOG_TAG, "Error reading settings.json. Falling back to default settings :"+ex.getMessage());
                writeSettingsJSON(settingsJSON);
            }


        } else {
            boolean created = settingsJSON.createNewFile();
            if (!created) {
                throw new IOException("There was a problem creating settings.json. " +
                        "Make sure you have permissions to create files in the current directory.");
            }
            Log.i(LOG_TAG, "Server started for the first time, creating settings.json in the server directory.");
            writeSettingsJSON(settingsJSON);

        }

    }

    private void writeSettingsJSON(File settingsJSON)throws IOException {

        if (settingsJSON.delete()) {
            Log.v(LOG_TAG, "Deleted settings.json");
        }
        if (settingsJSON.createNewFile()) {
            Log.v(LOG_TAG, "Created settings.json");
        }

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

    private void loadExistingUsers() {
        CompletableFuture<List<UserDetails>> future = new CompletableFuture<>();
        ServerExecutors.getDatabaseExecutor().submit(()->future.complete(database.fetchUserList()));
        future.thenApply(clients::addAll);
    }

    private void startServer() {

        try {

            Log.i(LOG_TAG, "Setting up and starting server...");
            goToSleep(5000);

            //Creating a new server on port serverPort
            messageServerSocket = new ServerSocket(serverPort);
            Log.i(LOG_TAG, String.format("Server started on port %d",serverPort));
            goToSleep(2000);

            initializeServerOperations();
            startAcceptingClients();
            Log.i(LOG_TAG, String.format("Accepting clients on port %d",serverPort));
            displayHelpMenu();

        } catch (IOException ex) {
            Log.e(LOG_TAG, "An error occurred while trying to start the server: " + ex.getMessage(), ex);
        }

    }

    //Start the thread that lets clients connect to the server
    private void startAcceptingClients() {

        ServerExecutors.getServerExecutor().submit(() -> {

            while (RUNNING.get()) {

                try {

                    Socket client = messageServerSocket.accept(); //Accept connections to the server
                        
                    //Creating a new thread to handle logging in
                    // so that client requests are not queued
                    ServerExecutors.getVerificationHandlerExecutor().submit(new VerificationHandler(client, gMailService));

                } catch (IOException ex) {
                    if (!ex.getMessage().equalsIgnoreCase("socket closed")) {
                        Log.e(LOG_TAG, "An error occurred: " + ex.getMessage(), ex);
                    }
                }
            }

        });

    }

    // Enable operating on the server while it is running
    private void initializeServerOperations() {

        ServerExecutors.getServerExecutor().submit(() -> {
            while (RUNNING.get()) {
                try {
                    String operation = STDIN.readLine();
                    if (operation.length() == 1) {
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
                            default:
                                Log.e(LOG_TAG, "Operation not recognised, enter ? for possible operations.");
                        }
                    } else
                        Log.e(LOG_TAG, "Operation not recognised, enter ? for possible operations.");

                } catch (IOException ex) {
                    Log.e(LOG_TAG, "An error occurred while trying to read from System.in", ex);
                }
            }
        });

    }

    //Cleans up and closes the server ;
    private void quitServer() throws IOException {

        verboseLogging = Log.verboseIsShown();
        Log.showVerbose(false);
        File settingsJSON = new File(SETTINGS_JSON_PATH);
        writeSettingsJSON(settingsJSON);
        Log.showVerbose(verboseLogging);

        Log.i(LOG_TAG, "Shutting down server...");
        goToSleep(2000);
        if (RUNNING.get()) {
            try {
                RUNNING.set(false);
                for (ClientHandler currentClient : activeClientMap.values()) {
                    if (currentClient.isLoggedIn) {
                        currentClient.disconnect(true);
                    }
                }
                ServerExecutors.close();
                messageServerSocket.close();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Server force closed: " + ex.getMessage(), ex);
            } finally {
                Log.i(LOG_TAG, "Server Shutdown, EXITING...");
            }
        }
    }

}