package org.shardav.server.comms;

public class Response {

    enum Status {
        SUCCESS,
        INVALID
    }

    Status status;
    String message;

    public Response(Status status) {
        this.status = status;
    }

    public Response(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
