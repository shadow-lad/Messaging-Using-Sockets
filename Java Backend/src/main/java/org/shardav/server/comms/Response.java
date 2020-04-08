package org.shardav.server.comms;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Response {

    public enum ResponseStatus {
        FAILED("failed"),
        INVALID("invalid"),
        SENT("sent"),
        SUCCESS("success");

        private String status;

        ResponseStatus(String status){
            this.status = status;
        }

        public String getValue() {
            return status;
        }
    }

    ResponseStatus responseStatus;
    String message;

    public Response(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        this.message = null;
    }

    public Response(ResponseStatus responseStatus, String message) {
        this.responseStatus = responseStatus;
        this.message = message;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponseStatus(ResponseStatus status){this.responseStatus = status;}

    public String toJSON(){
        return new JSONObject(this.toMap()).toString();
    }

    private Map<String, Object> toMap(){

        Map<String,Object> map = new HashMap<>();
        map.put("status",this.responseStatus.getValue());
        map.put("message",this.getMessage());
        return map;

    }

}
