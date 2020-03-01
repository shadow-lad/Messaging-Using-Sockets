package org.shardav.server.comms.messages;

import org.json.JSONObject;
import org.shardav.server.comms.login.LoginDetails;

import java.util.Map;

public class PrivateMessageDetails extends MessageDetails {

    private String recipient;

    /**
     * Private message without any media
     *
     * @param id id of the message, it should be unique for future purposes
     * @param message the message to be sent
     * @param timeStamp the time at which the message was sent
     * @param sender the sender of the message
     * @param recipient the recipient of the message
     */
    private PrivateMessageDetails(String id, String message, long timeStamp, String sender, String recipient) {
        super(id, message, timeStamp, sender);
        this.recipient = recipient;

    }

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
    private PrivateMessageDetails(String id, String message, String media, long timeStamp, String sender,String recipient) {
        super(id, message, media, timeStamp, sender);
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
        return object;
    }

    public static PrivateMessageDetails getInstance(JSONObject detailsObject){

        PrivateMessageDetails messageDetails = (PrivateMessageDetails) MessageDetails.getInstance(detailsObject);

        //TODO : Use proper error handling to implement fetching of PrivateMessageDetails object

        return null;
    }

}
