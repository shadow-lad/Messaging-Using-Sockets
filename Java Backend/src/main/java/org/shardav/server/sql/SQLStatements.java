package org.shardav.server.sql;

public class SQLStatements {

    public static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS socket_messaging;";
    public static final String USE_DATABASE = "USE socket_messaging;";

    // CREATE STATEMENTS

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
            "FOREIGN KEY (receiver) REFERENCES users(email) ON DELETE CASCADE," +
            "FOREIGN KEY (sender) REFERENCES users(email) ON DELETE CASCADE" +
            ");";

    public static final String CREATE_TABLE_USERS_FRIENDS = "CREATE TABLE IF NOT EXISTS users_friends (" +
            "user VARCHAR(100) NOT NULL," +
            "friend VARCHAR(100) NOT NULL," +
            "PRIMARY KEY (user, friend)," +
            "FOREIGN KEY (user) REFERENCES users(email) ON DELETE CASCADE," +
            "FOREIGN KEY (friend) REFERENCES users(email) ON DELETE CASCADE" +
            ");";

    // READ STATEMENTS

    public static final String VIEW_USER_LIST = "SELECT email, username from users;";

    public static final String FETCH_MESSAGES_BY_EMAIL = "SELECT * FROM private_messages WHERE receiver = ?;";

    public static final String FETCH_USER_DETAILS_BY_EMAIL = "SELECT username, password FROM users WHERE email = ?;";

    public static final String FETCH_USER_DETAILS_BY_USERNAME = "SELECT email, password FROM users WHERE username = ?;";

    public static final String FETCH_USER_FRIENDS = "SELECT friend FROM users WHERE email = ?;";

    // UPDATE STATEMENTS

    public static final String INSERT_PRIVATE_MESSAGE = "INSERT INTO private_messages " +
            "VALUES (?, ?, ?, ?, ?, ?);";

    public static final String INSERT_USER = "INSERT INTO users VALUES (?, ?, ?);";

    public static final String INSERT_USER_FRIENDS = "INSERT INTO users_friends VALUES (?, ?);";

    // DELETE STATEMENTS

    public static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE email = ?;";

    public static final String DELETE_MESSAGE_BY_ID = "DELETE FROM private_messages WHERE id = ?";

}
