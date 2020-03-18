package org.shardav.server.comms.login;

import org.json.JSONObject;
import org.shardav.server.comms.Details;

import java.util.HashMap;
import java.util.Map;

public class LoginDetails implements Details {

    private String username;
    private String password;
    private String email;

    /**
     * Constructor used to create an instance of login details
     * @param username username of the user
     * @param password md5 hashed password cause security (duh!)
     */
    private LoginDetails(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword () {
        return this.password;
    }

    public boolean hasUsername() {
        return this.username != null;
    }

    public boolean hasEmail() {
        return this.email != null;
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

        map.put("username",this.username);
        map.put("password",this.password);
        map.put("email",this.email);

        return map;
    }

    /**
     * Returns an instance of LoginDetails object
     *
     * @param detailsObject An object of type &lt;? extends JSONObject&gt;
     * @return An instance of LoginDetails class
     */
    public static LoginDetails getInstance(JSONObject detailsObject) throws IllegalArgumentException {

        if(detailsObject.has("username")
                && detailsObject.has("email")
                && detailsObject.has("password")
                && detailsObject.getString("password") != null
                && ( detailsObject.getString("username")!=null || detailsObject.getString("email")!=null)) {

            String username = detailsObject.getString("username");
            String password = detailsObject.getString("password");
            String email = detailsObject.getString("email");

            return new LoginDetails(username, password, email);

        } else
            throw new IllegalArgumentException("The JSONObject passed should contain keys username, password and email.");
    }

}
