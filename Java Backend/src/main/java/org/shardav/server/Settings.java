package org.shardav.server;

public class Settings {
    private Boolean verboseLogging;
    private Integer serverPort;
    private final MySQLCredentials mySQL;

    public Settings(Boolean verboseLogging, Integer serverPort, MySQLCredentials mySQL) {
        this.verboseLogging = verboseLogging;
        this.serverPort = serverPort;
        this.mySQL = mySQL;
    }

    public Boolean getVerboseLogging() {
        return verboseLogging;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public MySQLCredentials getMySQL() {
        return mySQL;
    }

    public void setVerboseLogging(Boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isNull() {
        return this.verboseLogging == null || this.mySQL == null ||
                this.mySQL.isNull() || this.serverPort == null;
    }

}

class MySQLCredentials {
    private String username;
    private String password;

    public MySQLCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected boolean isNull() {
        return this.username == null || this.password == null;
    }
}
