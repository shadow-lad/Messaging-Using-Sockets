package org.shardav.server.sql;

public class SQLStatements {

    public static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS socket_messaging;";
    public static final String USE_DATABASE = "USE socket_messaging;";

    public static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS users (" +
            "email VARCHAR(100) PRIMARY KEY NOT NULL," +
            "username VARCHAR(16) UNIQUE NOT NULL," +
            "password CHAR(32) NOT NULL" +
            ");";
    public static final String CREATE_TABLE_PRIVATE_MESSAGES = "CREATE TABLE IF NOT EXISTS private_messages (" +
            "id VARCHAR(100) PRIMARY KEY NOT NULL," +
            "receiver VARCHAR(100) NOT NULL," +
            "sender VARCHAR(100) NOT NULL," +
            "media VARCHAR(500) DEFAULT NULL," +
            "message VARCHAR(1000) DEFAULT NULL," +
            "time LONG NOT NULL," +
            "    FOREIGN KEY (receiver) REFERENCES users(email) ON DELETE CASCADE,\n" +
            "    FOREIGN KEY (sender) REFERENCES users(email) ON DELETE CASCADE\n" +
            ");";

    public static final String CREATE_TABLE_USERS_FRIENDS = "CREATE TABLE IF NOT EXISTS users_friends (" +
            "user VARCHAR(100) NOT NULL," +
            "friend VARCHAR(100) NOT NULL," +
            "PRIMARY KEY (user, friend)," +
            "FOREIGN KEY (user) REFERENCES users(email) ON DELETE CASCADE," +
            "FOREIGN KEY (friend) REFERENCES users(email) ON DELETE CASCADE" +
            ");";

    public static final String VIEW_USER_LIST = "SELECT email, username from users;";

    public static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE email = ?;";

    public static final String VIEW_MESSAGES_BY_EMAIL = "SELECT * FROM private_messages WHERE receiver = ?;";

    public static final String FETCH_USER_DETAILS_BY_EMAIL = "SELECT username, password FROM users WHERE email = ?";

    public static final String FETCH_USER_DETAILS_BY_USERNAME = "SELECT email, password FROM users WHERE username = ?";

    public static final String FETCH_USER_FRIENDS = "SELECT friend FROM users WHERE email = ?";

    public static final String INSERT_INTO_PRIVATE_MESSAGES_WITHOUT_MEDIA = "INSERT INTO private_messages(id, receiver, sender, message, time) VALUES (" +
            "\"%s\", \"%s\", \"%s\", \"%s\", \"%d\"" +
            ");";
    public static final String INSERT_INTO_PRIVATE_MESSAGES_WITHOUT_MESSAGES = "INSERT INTO private_messages(id, receiver, sender, media, time) VALUES (" +
            "\"%s\", \"%s\", \"%s\", \"%s\", \"%d\"" +
            ");";

    public static final String INSERT_INTO_PRIVATE_MESSAGES_WITH_MEDIA = "INSERT INTO private_messages(id, receiver, sender, media, message, time) VALUES (" +
            "\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%d\"" +
            ");";

    public static final String INSERT_USER = "INSERT INTO users VALUES (" +
            " \"%s\", \"%s\", \"%s\"" +
            ");";

    public static final String INSERT_USER_FRIENDS = "INSERT INTO users_friends VALUES (" +
            " \"%s\", \"%s\"" +
            "), (" +
            " \"%s\", \"%s\"" +
            ");";

}
