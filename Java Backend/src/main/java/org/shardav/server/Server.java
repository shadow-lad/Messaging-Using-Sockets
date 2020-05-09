package org.shardav.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.handler.ClientHandler;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    public static final Set<UserDetails> CLIENT_SET = new HashSet<>();
    public static final Map<String, ClientHandler> CLIENT_MAP = new HashMap<>(); // Map of all clients
    public static final Set<String> ACTIVE_CLIENTS = new HashSet<>();//List of all registered users.

    private static final String LOG_TAG = Server.class.getSimpleName();
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);
    private static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in));

    final String SETTINGS_JSON_PATH;

    //ServerSocket should be on at all times.
    private static ServerSocket messageServerSocket;

    private int serverPort;
    private String mySQLUsername, mySQLPassword;
    private boolean verboseLogging;

    // External service variables
    private final Database database;
    private final GMailService mailService;

    //Default constructor
    private Server() throws URISyntaxException, IOException,
            SQLException, InstantiationException, GeneralSecurityException {

        this.serverPort = 6969;
        this.mySQLUsername = "root";
        this.mySQLPassword = "toor";
        this.verboseLogging = false;

        this.mailService = GMailService.getInstance(); // Initializing GMailService

        final URI PATH = Server.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String directoryPath = Paths.get(PATH).toString();
        directoryPath = directoryPath.substring(0,directoryPath.lastIndexOf(File.separatorChar)+1);

        this.SETTINGS_JSON_PATH = directoryPath + "settings.json";

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

        } catch (URISyntaxException | IOException |
                SQLException | InstantiationException | GeneralSecurityException ex) {

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

    //Prints the active clients
    private void printActiveClients() {
        Log.i(LOG_TAG, "Active Clients: " + (ACTIVE_CLIENTS.size() == 0 ? "No Active Clients" : ""));
        for (String currentClient : ACTIVE_CLIENTS) {
            Log.i(LOG_TAG, currentClient);
        }
    }

    //Toggles verbose output
    private void toggleVerbose() {

        boolean previous = Log.verboseIsShown();
        Log.i(LOG_TAG, previous ? "Disabling verbose output." : "Enabling verbose output.");
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

    private void loadExistingUsers() throws SQLException {
        Log.i(LOG_TAG, "Loading existing users");
        List<UserDetails> userDetails = database.fetchUserList();
        for (UserDetails user : userDetails) {
            CLIENT_MAP.put(user.getEmail(), null);
            CLIENT_SET.add(user);
        }
        Log.i(LOG_TAG, "Loaded existing users");
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
                    ServerExecutors.getClientHandlerExecutor().submit(new ClientHandler(client, database, mailService));

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
                                shutdownServer();
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
    private void shutdownServer() throws IOException {

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
                for (String client : ACTIVE_CLIENTS) {
                    ClientHandler currentClient = CLIENT_MAP.get(client);
                    currentClient.disconnect(true);
                }
                ServerExecutors.close();
                messageServerSocket.close();
                database.close();
            } catch (IOException | SQLException ignore) {
            } finally {
                Log.i(LOG_TAG, "Server Shutdown, EXITING...");
            }
            System.exit(0);
        }
    }

}