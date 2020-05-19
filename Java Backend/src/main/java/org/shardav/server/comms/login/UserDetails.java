package org.shardav.server.comms.login;

import org.shardav.server.comms.Details;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDetails implements Details {

    private final String email;
    private final String username;
    private String password;
    private Set<UserDetails> friends;

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
        return this.email != null && !email.isEmpty();
    }

    public boolean hasUsername() {
        return this.username != null && !username.isEmpty();
    }

    public boolean hasPassword() {
        return this.password != null && !password.isEmpty();
    }

    public void setFriends(Collection<UserDetails> friends) {
        if (this.friends == null) {
            this.friends = new HashSet<>(friends);
        } else {
            this.friends.addAll(friends);
        }
    }

}
