package org.shardav.server.comms.messages;

import org.json.JSONObject;

public class GlobalMessageDetails extends MessageDetails {

    /**
     * Message without any media
     *
     * @param id the id of the message, it should be unique for future purposes
     * @param message the body of the message to be sent
     * @param timeStamp the time at which the message was sent
     * @param sender the person who sent the message
     */
    private GlobalMessageDetails(String id, String message, long timeStamp, String sender) {
        super(id, message, timeStamp, sender);
    }

    /**
     * Message with media
     * @param id the id of the message, it should be unique for future purposes
     * @param message the body of the message to be sent
     * @param media the media of the message
     * @param timeStamp the time at which the message was sent
     * @param sender the person who sent the message
     */
    private GlobalMessageDetails(String id, String message, String media, long timeStamp, String sender) {
        super(id, message, media, timeStamp, sender);
    }

    /**
     * Returns an instance of GlobalMessageDetails object
     *
     * @param detailsObject An object of type &lt;? extends JSONObject&gt;
     * @return An instance of GlobalMessageDetails class
     */
    public static GlobalMessageDetails getInstance(JSONObject detailsObject){
        return (GlobalMessageDetails) MessageDetails.getInstance(detailsObject);
    }



}
