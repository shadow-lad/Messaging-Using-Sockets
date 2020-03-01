package org.shardav.server.comms.messages;

import org.json.JSONObject;
import org.shardav.server.comms.Details;

import java.util.HashMap;
import java.util.Map;

class MessageDetails implements Details {

    private String id, message, media, sender;

    private long timeStamp;

    /**
     * Message without any media
     *
     * @param id the id of the message, it should be unique for future purposes
     * @param message the body of the message to be sent
     * @param timeStamp the time at which the message was sent
     * @param sender the person who sent the message
     */
    protected MessageDetails(String id, String message, long timeStamp, String sender) {
        this.id = id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.media = null;
        this.sender = sender;
    }

    /**
     * Message with media
     * @param id the id of the message, it should be unique for future purposes
     * @param message the body of the message to be sent
     * @param media the media of the message
     * @param timeStamp the time at which the message was sent
     * @param sender the person who sent the message
     */
    protected MessageDetails(String id, String message, String media, long timeStamp, String sender) {
        this(id,message,timeStamp, sender);
        this.media = media;
    }

    /**
     * Converts the attributes of the current object to a map and returns it.
     *
     * @return A map containing the details of the attributes of the object.
     */
    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();

        map.put("id",this.id);
        map.put("message",this.message);
        map.put("from",this.sender);
        map.put("media",this.media);
        map.put("time",this.timeStamp);

        return map;
    }

    /**
     * Fetch the id of the message
     * @return Returns the id of the current message.
     */
    public String getId() {
        return id;
    }

    /**
     * Fetch the body of the current message
     * @return Returns the body of the current message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Fetch the url of the media of the message
     * @return Returns the url of the media of the current message.
     */
    public String getMedia() {
        return media;
    }

    /**
     * Fetch the sender of the message
     * @return Returns the sender of the current message.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Fetch the timestamp of the message
     * @return Returns the timestamp of the current message.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns an instance of MessageDetails object
     *
     * @param detailsObject An object of type &lt;? extends JSONObject&gt;
     * @return An instance of MessageDetails class
     */
    protected static MessageDetails getInstance(JSONObject detailsObject)throws IllegalArgumentException {

        if(detailsObject.has("id") && detailsObject.getString("id")!=null
                && detailsObject.has("message")
                && detailsObject.has("time") && detailsObject.getLong("time")!=0
                && detailsObject.has("from")) {

            String id = detailsObject.getString("id");
            String message = detailsObject.getString("message");
            long timestamp = detailsObject.getLong("time");
            String sender = detailsObject.getString("sender");

            if(detailsObject.has("media")){
                String media = detailsObject.getString("media");
                return new MessageDetails(id,message,media,timestamp,sender);
            } else if (message!=null)
                return new MessageDetails(id,message,timestamp,sender);
            else
                throw new IllegalArgumentException("Message cannot be null");
        } else
            throw new IllegalArgumentException("JSONObject should contain 'id', 'message', 'time' and 'from' keys");

    }

}
