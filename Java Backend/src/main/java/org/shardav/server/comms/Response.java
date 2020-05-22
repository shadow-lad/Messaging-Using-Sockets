package org.shardav.server.comms;

public class Response<T> {

    public enum ResponseEvent {
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

    ResponseEvent event;
    ResponseType type;
    String message;
    T details;

    public Response (ResponseEvent event, ResponseType type) {
        this.event = event;
        this.type = type;
        this.message = null;
        this.details = null;
    }

    public Response (ResponseEvent event, ResponseType type, String message) {
        this.event = event;
        this.type = type;
        this.message = message;
        this.details = null;
    }

    public Response (ResponseEvent event, ResponseType type, T details) {
        this.event = event;
        this.type = type;
        this.message = null;
        this.details = details;
    }

    public ResponseEvent getEvent() {
        return event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEvent(ResponseEvent event){this.event = event;}

    public void setType(ResponseType type) {
        this.type = type;
    }

    public void setDetails(T details) {
        this.details = details;
    }
}
