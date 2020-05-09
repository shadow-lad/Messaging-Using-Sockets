package org.shardav.server.comms.login;

import org.json.JSONObject;
import org.shardav.server.comms.Request;

public class LoginRequest<T extends UserDetails> extends Request<T> {

    /**
     * Constructor used to create a new login request
     *
     * @param details An instance of login details containing the username and password
     */
    private LoginRequest(T details) {
        super(RequestType.login, details);
    }

    /**
     * Fetches the details of the current LoginRequest
     *
     * @return An instance of LoginDetails object
     */
    @Override
    public T getDetails() {
        return this.details;
    }

}
