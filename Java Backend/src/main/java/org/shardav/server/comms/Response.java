package org.shardav.server.comms;

public class Response<T> {

    public enum ResponseStatus {
        failed,
        invalid,
        sent,
        success
    }

    public enum ResponseType {
        general,
        login,
        logout,
        message,
        otp,
        registration,
        users,
        verify
    }

    ResponseStatus status;
    ResponseType type;
    String message;
    T details;

    public Response (ResponseStatus status, ResponseType type) {
        this.status = status;
        this.type = type;
        this.message = null;
        this.details = null;
    }

    public Response (ResponseStatus status, ResponseType type, String message) {
        this.status = status;
        this.type = type;
        this.message = message;
        this.details = null;
    }

    public Response (ResponseStatus status, ResponseType type, T details) {
        this.status = status;
        this.type = type;
        this.message = null;
        this.details = details;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(ResponseStatus status){this.status = status;}

    public void setType(ResponseType type) {
        this.type = type;
    }

    public void setDetails(T details) {
        this.details = details;
    }
}
