package org.shardav.server.comms.messages;

import org.shardav.server.comms.Request;

public class Message<T extends MessageDetails> extends Request<T> {

    /**
     * A custom data type created to support the types of messages that can be sent
     */
    public enum MessageType{
        global,
        personal
    }

    /**
     * Current messageType
     */
    private MessageType type;

    /**
     * Constructor used to create a message
     *
     * @param details The details of the message of type &lt;? extends MessageDetails&gt;
     */
    public Message( T details ) {
        super(RequestType.message, details);
        if(details instanceof PersonalMessageDetails)
            this.type = MessageType.personal;
        else if(details instanceof GlobalMessageDetails)
            this.type = MessageType.global;

    }

    /**
     * Get the current messageType
     * @return A value of enum MessageType representing the current MessageType.
     */
    public MessageType getType() {
        return this.type;
    }

    /**
     * Fetch the details of the current message object
     *
     * @return An object of type MessageDetails
     */
    @Override
    public T getDetails() {
        return this.details;
    }

}
