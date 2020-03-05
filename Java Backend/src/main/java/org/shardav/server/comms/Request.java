package org.shardav.server.comms;

import java.util.HashMap;
import java.util.Map;

public abstract class Request {

    public enum RequestType {
        LOGIN("login"),
        LOGOUT("logout"),
        MESSAGE("message");

        private final String requestType;

        RequestType(String requestType){
            this.requestType = requestType;
        }

        public String getValue(){return requestType;}

    }

    protected RequestType requestType;

    protected Details details;

    /**
     * Constructor used to create a Request
     *
     * @param requestType The type of request
     * @param details The details of the request
     */
    public Request(RequestType requestType, Details details) {
        this.requestType = requestType;
        this.details = details;
    }

    /**
     * Fetch the type of request
     * @return Returns a value of type RequestType representing the current RequestType.
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Converts the attributes of the current object to a map and returns it.
     *
     * @return A map containing the details of the attributes of the object.
     */
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();

        map.put("request",getRequestType().getValue());
        map.put("details",getDetails().toMap());

        return map;
    }

    /**
     * Returns an instance of details which contains the relevant details of the request
     *
     * @return Returns the details of the object
     */
    public abstract Details getDetails();
}
