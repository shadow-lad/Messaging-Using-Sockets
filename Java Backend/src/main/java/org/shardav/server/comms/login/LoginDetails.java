package org.shardav.server.comms.login;

import org.json.JSONObject;
import org.shardav.server.comms.Details;

import java.util.HashMap;
import java.util.Map;

public class LoginDetails implements Details {

    private String username;
    private String password;

    /**
     * Constructor used to create an instance of login details
     * @param username username of the user
     * @param password md5 hashed password cause security (duh!)
     */
    private LoginDetails(String username, String password) {
        this.username = username;
        this.password = password;
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

        return map;
    }

    /**
     * Returns an instance of LoginDetails object
     *
     * @param detailsObject An object of type &lt;? extends JSONObject&gt;
     * @return An instance of LoginDetails class
     */
    public static LoginDetails getInstance(JSONObject detailsObject){
        //TODO : Use proper error handling to implement fetching of LoginDetails object
        return null;
    }

}
