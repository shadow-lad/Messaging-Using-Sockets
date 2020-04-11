package org.shardav.server.comms.login;

import org.json.JSONObject;
import org.shardav.server.comms.Details;

import java.util.HashMap;
import java.util.Map;

public class UserDetails implements Details {

    private String email, username, password;

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

    /**
     * Converts the attributes of the current object to a map and returns it.
     *
     * @return A map containing the details of the attributes of the object.
     */
    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();

        map.put("email",this.email);
        map.put("username",this.username);
        map.put("password",this.password);

        return map;
    }

    /**
     * Returns an instance of LoginDetails object
     *
     * @param detailsObject An object of type &lt;? extends JSONObject&gt;
     * @return An instance of LoginDetails class
     */
    public static UserDetails getInstance(JSONObject detailsObject) throws IllegalArgumentException {

        if((detailsObject.has("email") || detailsObject.has("username"))
                && detailsObject.has("password")
                && detailsObject.getString("password") != null) {

            String username = detailsObject.has("username") ? detailsObject.getString("username") : null;
            String password = detailsObject.getString("password");
            String email = detailsObject.has("email") ? detailsObject.getString("email") : null;

            return new UserDetails(email, username, password);

        } else
            throw new IllegalArgumentException("The JSONObject passed should contain keys username, password and email.");
    }

}
