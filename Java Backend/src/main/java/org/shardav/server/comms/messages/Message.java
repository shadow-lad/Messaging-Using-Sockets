package org.shardav.server.comms.messages;

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
     * @param details The details of the message which can be an instance of subclass of MessageDetails
     */
    public Message( MessageDetails details ) {
        super(RequestType.MESSAGE, details);
        if(details instanceof PrivateMessageDetails)
            this.messageType = MessageType.PRIVATE;
        else
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

}
