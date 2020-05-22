package org.shardav.server.comms.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private MessageType request;

    /**
     * Constructor used to create a message
     *
     * @param details The details of the message of type &lt;? extends MessageDetails&gt;
     */
    public Message( T details ) {
        super(RequestType.message, details);
        if(details instanceof PersonalMessageDetails)
            this.request = MessageType.personal;
        else if(details instanceof GlobalMessageDetails)
            this.request = MessageType.global;

    }

    /**
     * Get the current messageType
     * @return A value of enum MessageType representing the current MessageType.
     */
    public MessageType getRequest() {
        return this.request;
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

    public static void main(String[] args) {
        Message<PersonalMessageDetails> message = new Message<>(new PersonalMessageDetails ("id","Hello World", "https://url-to-firestore", 1234567890, "xyz@abc.com", "def@abc.com"));
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(message));
    }

}
