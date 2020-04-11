package org.shardav.server.comms.messages;

import org.json.JSONObject;

import java.util.Map;

public class PrivateMessageDetails extends MessageDetails {

    private String recipient;

    /**
     * Private message with media
     *
     * @param id id of the message, it should be unique for future purposes
     * @param message the message to be sent
     * @param media the media attached to the message
     * @param timeStamp the time at which the message was sent
     * @param sender the sender of the message
     * @param recipient the recipient of the message
     */
    public PrivateMessageDetails(String id, String message, String media, long timeStamp, String sender,String recipient) {
        super(id, message, media, timeStamp, sender);
        this.recipient = recipient;
    }

    private PrivateMessageDetails(String id, String message, String media, long timeStamp, String sender) {
        super(id, message, media, timeStamp, sender);
        this.recipient = null;
    }

    private PrivateMessageDetails(String id, String message, long timeStamp, String sender) {
        super(id, message, timeStamp, sender);
        this.recipient = null;

    }

    private void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Fetch the recipient of the message
     * @return Returns the recipient of the current message.
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Converts the attributes of the current object to a map and returns it.
     *
     * @return A map containing the details of the attributes of the object.
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> object = super.toMap();
        object.put("to",this.recipient);
        object.remove("from");
        return object;
    }

    /**
     * Returns an instance of PrivateMessageDetails object
     *
     * @param detailsObject An object of type &lt;? extends JSONObject&gt;
     * @return An instance of PrivateMessageDetails class
     */
    public static PrivateMessageDetails getInstance(JSONObject detailsObject) throws IllegalArgumentException {

        if(detailsObject.has("id") && detailsObject.getString("id")!=null
                && detailsObject.has("message")
                && detailsObject.has("time") && detailsObject.getLong("time")!=0
                && detailsObject.has("from")) {

            String id = detailsObject.getString("id");
            String message = detailsObject.getString("message");
            long timestamp = detailsObject.getLong("time");
            String sender = detailsObject.getString("from");

            PrivateMessageDetails details;
            if(detailsObject.has("media") && detailsObject.get("media") != null){
                String media = detailsObject.getString("media");
                details = new PrivateMessageDetails(id,message,media,timestamp,sender);
            } else if (message!=null) {
                details = new PrivateMessageDetails(id, message, timestamp, sender);
            } else {
                throw new IllegalArgumentException("Message cannot be null");
            }

            if(detailsObject.has("to") && detailsObject.getString("to")!=null){
                details.setRecipient(detailsObject.getString("to"));
                return details;
            } else
                throw new IllegalArgumentException("private messages should contain the key 'to'");

        } else {
            throw new IllegalArgumentException("JSONObject should contain 'id', 'message', 'time' and 'from' keys");
        }



    }

}
