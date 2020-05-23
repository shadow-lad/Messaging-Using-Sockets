package org.shardav.server.sql;

import org.shardav.server.Server;
import org.shardav.server.comms.login.UserDetails;
import org.shardav.server.comms.messages.Message;
import org.shardav.server.comms.messages.PersonalMessageDetails;
import org.shardav.utils.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private final Connection connection;
    private static Database instance;

    // READ PREPARED STATEMENTS

    private final PreparedStatement FETCH_MESSAGES_BY_EMAIL;
    private final PreparedStatement FETCH_USER_FRIENDS;
    private final PreparedStatement FETCH_USER_DETAILS_BY_EMAIL;
    private final PreparedStatement FETCH_USER_DETAILS_BY_USERNAME;

    // UPDATE PREPARED STATEMENTS

    private final PreparedStatement INSERT_PRIVATE_MESSAGE;
    private final PreparedStatement INSERT_INTO_USER_FRIENDS;
    private final PreparedStatement INSERT_USER;

    // DELETE PREPARED STATEMENTS

    private final PreparedStatement DELETE_USER_BY_EMAIL;
    private final PreparedStatement DELETE_MESSAGE_BY_ID;

    private static final Object LOCK = new Object();

    private static final String LOG_TAG = Server.class.getSimpleName() + ": " + Database.class.getSimpleName();

    private Database(String host, String port, String username, String password) throws SQLException {

        final String MYSQL_BASE_URL_FORMAT = "jdbc:mysql://%s:%s";

        connection = DriverManager.getConnection(String.format(MYSQL_BASE_URL_FORMAT, host, port), username, password);
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();

        statement.executeUpdate(SQLStatements.CREATE_DATABASE);
        statement.executeUpdate(SQLStatements.USE_DATABASE);

        // CREATE STATEMENTS

        statement.executeUpdate(SQLStatements.CREATE_TABLE_USERS);
        statement.executeUpdate(SQLStatements.CREATE_TABLE_PRIVATE_MESSAGES);
        statement.executeUpdate(SQLStatements.CREATE_TABLE_USERS_FRIENDS);

        // READ STATEMENTS

        FETCH_MESSAGES_BY_EMAIL = connection.prepareStatement(SQLStatements.FETCH_MESSAGES_BY_EMAIL);
        FETCH_USER_DETAILS_BY_EMAIL = connection.prepareStatement(SQLStatements.FETCH_USER_DETAILS_BY_EMAIL);
        FETCH_USER_DETAILS_BY_USERNAME = connection.prepareStatement(SQLStatements.FETCH_USER_DETAILS_BY_USERNAME);
        FETCH_USER_FRIENDS = connection.prepareStatement(SQLStatements.FETCH_USER_FRIENDS);

        // UPDATE STATEMENTS

        INSERT_PRIVATE_MESSAGE = connection.prepareStatement(SQLStatements.INSERT_PRIVATE_MESSAGE);
        INSERT_INTO_USER_FRIENDS = connection.prepareStatement(SQLStatements.INSERT_USER_FRIENDS);
        INSERT_USER = connection.prepareStatement(SQLStatements.INSERT_USER);

        // DELETE STATEMENTS

        DELETE_USER_BY_EMAIL = connection.prepareStatement(SQLStatements.DELETE_USER_BY_EMAIL);
        DELETE_MESSAGE_BY_ID = connection.prepareStatement(SQLStatements.DELETE_MESSAGE_BY_ID);

    }

    public static Database getInstance(String username, String password) throws SQLException, InstantiationException {
        return getInstance("localhost", username, password);
    }

    public static Database getInstance(String host, String username, String password) throws SQLException, InstantiationException {
        return getInstance(host, "3306", username, password);
    }

    public static Database getInstance(String host, String port, String username, String password) throws SQLException, InstantiationException {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new Database(host, port, username, password);
                return instance;
            } else {
                throw new InstantiationException("An instance of the database already exists");
            }
        }
    }

    public static Database getInstance() throws InstantiationException {
        synchronized (LOCK) {
            if (instance != null) {
                return instance;
            } else {
                throw new InstantiationException("An instance of the database already exists");
            }
        }
    }

    public void addMessage(PersonalMessageDetails details) throws SQLException {

        INSERT_PRIVATE_MESSAGE.clearParameters();

        INSERT_PRIVATE_MESSAGE.setString(1, details.getId());
        INSERT_PRIVATE_MESSAGE.setString(2, details.getTo());
        INSERT_PRIVATE_MESSAGE.setString(3, details.getFrom());
        INSERT_PRIVATE_MESSAGE.setString(4, details.getMedia());
        INSERT_PRIVATE_MESSAGE.setString(5, details.getMessage());
        INSERT_PRIVATE_MESSAGE.setLong(6, details.getTime());

        int rows = INSERT_PRIVATE_MESSAGE.executeUpdate();

        Log.v(LOG_TAG, "Message " + details.getId() + " inserted into database, " + rows + " affected.");

    }

    public void addUserFriends (String firstEmail, String secondEmail) {
        try {
            INSERT_INTO_USER_FRIENDS.clearParameters();
            INSERT_INTO_USER_FRIENDS.setString(1, firstEmail);
            INSERT_INTO_USER_FRIENDS.setString(2, secondEmail);
            INSERT_INTO_USER_FRIENDS.executeUpdate();
            INSERT_INTO_USER_FRIENDS.clearParameters();
            INSERT_INTO_USER_FRIENDS.setString(1, secondEmail);
            INSERT_INTO_USER_FRIENDS.setString(2, firstEmail);
            INSERT_INTO_USER_FRIENDS.executeUpdate();
        } catch (SQLException ignore){}
    }

    public List<String> getFriends (String email) {

        List<String> arrayList = new ArrayList<>();

        try {

            FETCH_USER_FRIENDS.clearParameters();
            FETCH_USER_FRIENDS.setString(1, email);

            ResultSet resultSet = FETCH_USER_FRIENDS.executeQuery();
            while (resultSet.next()) {
                arrayList.add(resultSet.getString("friend"));
            }

            resultSet.close();

        } catch (SQLException ignore){}

        return arrayList;
    }

    public void deleteUserByEmail(String email) throws SQLException {
        DELETE_USER_BY_EMAIL.clearParameters();
        DELETE_USER_BY_EMAIL.setString(1, email);

        DELETE_USER_BY_EMAIL.execute();
    }

    public void insertUser(UserDetails details) throws SQLException {

        INSERT_USER.clearParameters();
        INSERT_USER.setString(1, details.getEmail());
        INSERT_USER.setString(2, details.getUsername());
        INSERT_USER.setString(3, details.getPassword());
        int rows = INSERT_USER.executeUpdate();

        Log.v(LOG_TAG, "User " + details.getUsername() + " inserted into database, " + rows + " affected.");

    }

    public List<Message<PersonalMessageDetails>> fetchMessagesByEmail(String email) throws SQLException {
        List<Message<PersonalMessageDetails>> messages = new ArrayList<>();

        FETCH_MESSAGES_BY_EMAIL.clearParameters();
        FETCH_MESSAGES_BY_EMAIL.setString(1, email);

        ResultSet result = FETCH_MESSAGES_BY_EMAIL.executeQuery();

        while (result.next()) {
            messages.add(new Message<>(new PersonalMessageDetails(
                    result.getString("id"), //ID
                    result.getString("message"), //Message
                    result.getString("media"), //Media
                    result.getLong("time"), //Timestamp
                    result.getString("sender"), //Sender
                    result.getString("receiver")  //Recipient
            )));
        }

        result.close();

        return messages;

    }

    public void deleteMessageById(String id) throws SQLException {

        DELETE_MESSAGE_BY_ID.clearParameters();

        DELETE_MESSAGE_BY_ID.setString(1, id);

        DELETE_MESSAGE_BY_ID.executeUpdate();

    }

    public UserDetails fetchUserDetailsByUsername(String username) throws SQLException {

        FETCH_USER_DETAILS_BY_USERNAME.clearParameters();
        FETCH_USER_DETAILS_BY_USERNAME.setString(1, username);

        ResultSet result = FETCH_USER_DETAILS_BY_USERNAME.executeQuery();

        UserDetails userDetails = null;

        if (result.next())
            userDetails = new UserDetails(
                    result.getString("email"),
                    username,
                    result.getString("password")
            );

        result.close();

        return userDetails;

    }

    public UserDetails fetchUserDetailsByMail(String email) throws SQLException, IllegalArgumentException {

        FETCH_USER_DETAILS_BY_EMAIL.clearParameters();
        FETCH_USER_DETAILS_BY_EMAIL.setString(1, email);

        ResultSet result = FETCH_USER_DETAILS_BY_EMAIL.executeQuery();

        UserDetails userDetails = null;

        if (result.next()) {
            userDetails = new UserDetails(
                    email,
                    result.getString("username"),
                    result.getString("password")
            );
        }

        result.close();

        return userDetails;

    }

    public List<UserDetails> fetchUserList() throws SQLException {

        List<UserDetails> users = new ArrayList<>();

        Statement viewUserList = connection.createStatement();

        ResultSet result = viewUserList.executeQuery(SQLStatements.VIEW_USER_LIST);

        while (result.next()) {
            users.add(new UserDetails(
                    result.getString("email"),
                    result.getString("username")
            ));
        }

        result.close();

        return users;

    }

    public void close() throws SQLException {

        instance = null;
        if (!connection.isClosed())
            connection.close();

    }

}
