package org.shardav.server.comms;

public abstract class Request<T extends Details> {

    public enum RequestType {
        login,
        logout,
        message,
        registration,
        users,
        verify

    }

    protected RequestType type;

    protected T details;

    /**
     * Constructor used to create a Request
     *
     * @param type The type of request
     * @param details The details of the request
     */
    public Request(RequestType type, T details) {
        this.type = type;
        this.details = details;
    }

    /**
     * Fetch the type of request
     * @return Returns a value of type RequestType representing the current RequestType.
     */
    public RequestType getType() {
        return this.type;
    }

    /**
     * Returns an instance of details which contains the relevant details of the request
     *
     * @return Returns the details of the object
     */
    public abstract T getDetails();
}
