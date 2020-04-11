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
    public static GlobalMessageDetails getInstance(JSONObject detailsObject) throws IllegalArgumentException {
        if(detailsObject.has("id") && detailsObject.getString("id")!=null
                && detailsObject.has("message")
                && detailsObject.has("time") && detailsObject.getLong("time")!=0
                && detailsObject.has("from")) {

            String id = detailsObject.getString("id");
            String message = detailsObject.getString("message");
            long timestamp = detailsObject.getLong("time");
            String sender = detailsObject.getString("from");

            if(detailsObject.has("media") && detailsObject.get("media") != null){
                String media = detailsObject.getString("media");
                return new GlobalMessageDetails(id,message,media,timestamp,sender);
            } else if (message!=null) {
                return new GlobalMessageDetails(id, message, timestamp, sender);
            } else {
                throw new IllegalArgumentException("Message cannot be null");
            }
        } else {
            throw new IllegalArgumentException("JSONObject should contain 'id', 'message', 'time' and 'from' keys");
        }
    }

}
