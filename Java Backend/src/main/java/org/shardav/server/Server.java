package org.shardav.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public static final Map<String, UserDetails> CLIENT_DETAILS_MAP = new HashMap<>();
    public static final Map<String, ClientHandler> CLIENT_MAP = new HashMap<>(); // Map of all clients
    public static final Set<String> ACTIVE_CLIENTS = new HashSet<>();//List of all registered users.

    private static final String LOG_TAG = Server.class.getSimpleName();
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);
    private static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in));

    final String SETTINGS_JSON_PATH;

    //ServerSocket should be on at all times.
    private static ServerSocket messageServerSocket;

    private final Settings settings;

    // External service variables
    private final Database database;
    private final GMailService mailService;

    //Default constructor
    private Server() throws URISyntaxException, IOException,
            SQLException, InstantiationException, GeneralSecurityException {

        this.settings = new Settings(false, 6969, new MySQLCredentials("root", "toor"));


        this.mailService = GMailService.getInstance(); // Initializing GMailService

        final URI PATH = Server.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String directoryPath = Paths.get(PATH).toString();
        directoryPath = directoryPath.substring(0,directoryPath.lastIndexOf(File.separatorChar)+1);

        this.SETTINGS_JSON_PATH = directoryPath + "settings.json";

        getServerConfig(); // Load the configuration from the settings.json file
        database = Database.getInstance(settings.getMySQL().getUsername(), settings.getMySQL().getPassword());

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
        Log.i(LOG_TAG,
                "\n--------" +
                        "\n  HELP" +
                        "\n--------" +
                        "\n\n" +
                        "l - List connected clients\n" +
                        "v - Toggle verbose logging\n" +
                        "q - Shutdown server\n" +
                        "? - Show this menu\n");
    }

    //Prints the active clients
    private void printActiveClients() {
        StringBuilder activeClients = new StringBuilder(ACTIVE_CLIENTS.size() == 0 ? "\nNo Active Clients" : "\nActive Clients:");
        for (String currentClient : ACTIVE_CLIENTS) {
            activeClients.append('\n').append(currentClient);
        }
        Log.i(LOG_TAG, activeClients.toString());

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

            while((line=br.readLine())!=null) {
                json.append(line.trim());
            }

            Settings loadedSettings = new Gson().fromJson(json.toString(), Settings.class);

            if (!(loadedSettings == null || loadedSettings.isNull())) {

                this.settings.setServerPort(loadedSettings.getServerPort());
                this.settings.setVerboseLogging(loadedSettings.getVerboseLogging());
                this.settings.getMySQL().setUsername(loadedSettings.getMySQL().getUsername());
                this.settings.getMySQL().setPassword(loadedSettings.getMySQL().getPassword());

                Log.showVerbose(this.settings.getVerboseLogging());

                Log.i(LOG_TAG, "Settings loaded");

            } else {
                Log.e(LOG_TAG, "Error reading settings.json, starting initial setup...");
                initialSetup();
            }


        } else {

            if (!settingsJSON.createNewFile()) {
                throw new IOException("There was a problem creating settings.json. " +
                        "Make sure you have permissions to create files in the current directory.");
            }
            Log.i(LOG_TAG, "Server started for the first time, starting initial setup...");
            initialSetup();

        }

    }

    private void initialSetup() throws IOException {
        goToSleep(2000);
        File settingsJSON = new File(SETTINGS_JSON_PATH);
        int serverPort;
        try {
            System.out.print("Port on which the server to host(default=6969): ");
            serverPort = Integer.parseInt(STDIN.readLine());
        } catch (NumberFormatException ex) {
            serverPort = 6969;
        }
        this.settings.setServerPort(serverPort);

        System.out.print("Show verbose logging (Y/N)(default=N): ");
        String verboseLogging = STDIN.readLine().toUpperCase();
        char verbose;

        if (verboseLogging.length() == 1) {
            verbose = verboseLogging.charAt(0);
        } else {
            verbose = 'N';
        }
        switch (verbose) {
            case 'Y': this.settings.setVerboseLogging(true);
            default:
                break;
            case 'N': this.settings.setVerboseLogging(false);
        }

        System.out.print("MySQL username (default=root): ");
        String mySQLUsername = STDIN.readLine();
        if (mySQLUsername == null || mySQLUsername.trim().isEmpty()) {
            mySQLUsername = "root";
        }
        this.settings.getMySQL().setUsername(mySQLUsername);

        System.out.print("MySQL password (default=toor): ");
        String mySQLPassword = STDIN.readLine();
        if (mySQLPassword.trim().isEmpty()) {
            mySQLPassword = "toor";
        }
        this.settings.getMySQL().setPassword(mySQLPassword);

        writeSettingsJSON(settingsJSON);
    }

    private void writeSettingsJSON(File settingsJSON)throws IOException {

        if (settingsJSON.delete()) {
            Log.v(LOG_TAG, "Deleted settings.json");
        }
        if (settingsJSON.createNewFile()) {
            Log.v(LOG_TAG, "Created settings.json");
        }

        PrintWriter pw = new PrintWriter(settingsJSON);
        Gson prettyGSON = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        pw.println(prettyGSON.toJson(this.settings));
        pw.flush();
        pw.close();

    }

    private void loadExistingUsers() throws SQLException {
        Log.i(LOG_TAG, "Loading existing users");
        List<UserDetails> userDetails = database.fetchUserList();
        for (UserDetails user : userDetails) {
            CLIENT_MAP.put(user.getEmail(), null);
            CLIENT_DETAILS_MAP.put(user.getEmail(), user);
        }
        Log.i(LOG_TAG, "Loaded existing users");
    }

    private void startServer() {

        try {

            Log.i(LOG_TAG, "Setting up and starting server...");
            goToSleep(5000);

            //Creating a new server on port serverPort
            messageServerSocket = new ServerSocket(this.settings.getServerPort());
            Log.i(LOG_TAG, String.format("Server started on port %d", this.settings.getServerPort()));
            goToSleep(2000);

            initializeServerOperations();
            startAcceptingClients();
            Log.i(LOG_TAG, String.format("Accepting clients on port %d", this.settings.getServerPort()));
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
                    Log.i(LOG_TAG, "A new client connected : " + client.getInetAddress());
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

        this.settings.setVerboseLogging(Log.verboseIsShown());
        File settingsJSON = new File(SETTINGS_JSON_PATH);
        writeSettingsJSON(settingsJSON);

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