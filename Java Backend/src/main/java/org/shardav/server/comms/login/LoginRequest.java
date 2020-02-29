package org.shardav.server.comms.login;

import org.shardav.server.comms.Request;

public class LoginRequest extends Request {

    public LoginRequest(LoginDetails details) {
        super(RequestType.LOGIN, details);
    }

    @Override
    public LoginDetails getDetails() {
        return (LoginDetails) super.getDetails();
    }
}
