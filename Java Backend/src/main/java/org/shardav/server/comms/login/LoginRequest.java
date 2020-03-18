package org.shardav.server.comms.login;

import org.json.JSONObject;
import org.shardav.server.comms.Request;

public class LoginRequest extends Request {

    /**
     * Constructor used to create a new login request
     *
     * @param details An instance of login details containing the username and password
     */
    private LoginRequest(LoginDetails details) {
        super(RequestType.LOGIN, details);
    }

    /**
     * Fetches the details of the current LoginRequest
     *
     * @return An instance of LoginDetails object
     */
    @Override
    public LoginDetails getDetails() {
        return (LoginDetails) this.details;
    }

    /**
     * Fetches an instance of LoginRequest from the JSONObject provided
     *
     * @param loginObject A object of type JSONObject that represents a login request
     * @return An instance of class login request with all the fields satisfied
     * @throws IllegalArgumentException Thrown if the request is invalid
     */
    public static LoginRequest getInstance(JSONObject loginObject)throws IllegalArgumentException {

        if(loginObject.has("request") && loginObject.getString("request")!=null
                && loginObject.has("details") && loginObject.getJSONObject("details")!=null){
            RequestType request = RequestType.getRequestType(loginObject.getString("request"));
            if(request == RequestType.LOGIN){
                return new LoginRequest(LoginDetails.getInstance(loginObject.getJSONObject("details")));
            } else if (request == RequestType.REGISTRATION){
                LoginDetails details = LoginDetails.getInstance(loginObject.getJSONObject("details"));
                if(details.hasPassword() && details.getEmail() != null && details.getUsername()!=null)
                    return new LoginRequest(details);
                else
                    throw new IllegalArgumentException("Invalid registration request, email, username and password should be present.");
            } else {
                throw new IllegalArgumentException("Invalid request type. First request should be login");
            }
        } else
            throw new IllegalArgumentException("Keys 'request' or 'details' is either not present or is null");

    }

}
