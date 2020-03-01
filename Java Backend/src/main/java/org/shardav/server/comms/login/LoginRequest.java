package org.shardav.server.comms.login;

import org.shardav.server.comms.Request;

public class LoginRequest extends Request {

    /**
     * Constructor used to create a new login request
     *
     * @param details An instance of login details containing the username and password
     */
    public LoginRequest(LoginDetails details) {
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
}
