package org.shardav.server.comms.login;

import org.shardav.server.comms.Details;

public class UserDetails implements Details {

    private final String email;
    private final String username;
    private String password;

    public UserDetails(String email, String username){
        this(email,username,null);
    }

    /**
     * Constructor used to create an instance of login details
     * @param username username of the user
     * @param email Email Id of the user, so that the server does not get overloaded :)
     * @param password md5 hashed password cause security (duh!)
     */
    public UserDetails(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword () {
        return this.password;
    }

    public boolean hasEmail() {
        return this.email != null;
    }

    public boolean hasUsername() {
        return this.username != null;
    }

    public boolean hasPassword() {
        return this.password != null;
    }

}
