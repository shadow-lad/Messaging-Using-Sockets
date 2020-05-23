package org.shardav.server.comms.messages;

import org.shardav.server.comms.Request;

public class Message<T extends MessageDetails> extends Request<T> {

    /**
     * Current messageType
     */
    private MessageEvent event;

    /**
     * Constructor used to create a message
     *
     * @param details The details of the message of type &lt;? extends MessageDetails&gt;
     */
    public Message(T details) {
        super(RequestType.message, details);
        if (details instanceof PersonalMessageDetails)
            this.event = MessageEvent.personal;
        else if (details instanceof GlobalMessageDetails)
            this.event = MessageEvent.global;

    }

    /**
     * Get the current messageType
     *
     * @return A value of enum MessageType representing the current MessageType.
     */
    public MessageEvent getEvent() {
        return this.event;
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

    /**
     * A custom data type created to support the types of messages that can be sent
     */
    public enum MessageEvent {
        global,
        personal
    }

}
