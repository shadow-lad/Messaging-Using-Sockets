package org.shardav.server.comms.login;

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

    public static LoginRequest getInstance(){

        //TODO : Use proper error handling to return a valid LoginDetails object be it global or private

        return null;
    }

}
