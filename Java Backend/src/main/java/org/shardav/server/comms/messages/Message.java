package org.shardav.server.comms.messages;

import org.json.JSONObject;
import org.shardav.server.comms.Request;

import java.util.Map;

public class Message extends Request {

    /**
     * A custom data type created to support the types of messages that can be sent
     */
    public enum MessageType{
        GLOBAL("global"),
        PRIVATE("private");

        private String messageType;

        MessageType(String type){
            this.messageType = type;
        }

        /**
         * Returns the value of the current enum value
         *
         * @return A string containing the value of the current enum variable
         */
        public String getValue(){
            return this.messageType;
        }

    }

    /**
     * Current messageType
     */
    private MessageType messageType;

    /**
     * Constructor used to create a message
     *
     * @param details The details of the message of type &lt;? extends MessageDetails&gt;
     */
    private Message( MessageDetails details ) {
        super(RequestType.MESSAGE, details);
        if(details instanceof PrivateMessageDetails)
            this.messageType = MessageType.PRIVATE;
        else if(details instanceof GlobalMessageDetails)
            this.messageType = MessageType.GLOBAL;

    }

    /**
     * Converts the attributes of the current object to a map and returns it.
     *
     * @return A map containing the details of the attributes of the object.
     */
    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> map = super.toMap();
        map.put("type",this.getMessageType().getValue());

        return map;
    }

    /**
     * Get the current messageType
     * @return A value of enum MessageType representing the current MessageType.
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Fetch the details of the current message object
     *
     * @return An object of type MessageDetails
     */
    @Override
    public MessageDetails getDetails() {
        return (MessageDetails) this.details;
    }

    public static Message getInstance(JSONObject messageObject){
        //TODO: Use proper error handling to return a valid Message object be it global or private
        return null;
    }

}
