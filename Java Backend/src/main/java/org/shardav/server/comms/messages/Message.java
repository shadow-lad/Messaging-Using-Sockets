package org.shardav.server.comms.messages;

import org.shardav.server.comms.Request;

public abstract class Message extends Request {

    enum MessageType{
        GLOBAL("global"),
        PRIVATE("private");

        private String messageType;

        MessageType(String type){
            this.messageType = type;
        }

        public String getMessageType(){
            return this.messageType;
        }

    }

    MessageType messageType;

    public Message(MessageDetails details, MessageType messageType) {
        super(RequestType.MESSAGE, details);
        this.messageType = messageType;
    }


    public abstract MessageDetails getDetails(); // TODO: Implement this appropriately

    public MessageType getMessageType() {
        return messageType;
    }
}
