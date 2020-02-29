package org.shardav.server.comms;

public abstract class Request {

    public enum RequestType {
        LOGIN,
        MESSAGE
    }

    protected RequestType requestType;

    protected Object details;

    public Request(RequestType requestType, Object details) {
        this.requestType = requestType;
        this.details = details;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public abstract Object getDetails();
}
