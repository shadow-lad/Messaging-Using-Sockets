package org.shardav.server.comms.messages;

public class GlobalMessageDetails extends MessageDetails {

    /**
     * Message with media
     * @param message the body of the message to be sent
     * @param media the media of the message
     * @param timeStamp the time at which the message was sent
     * @param from the person who sent the message
     */
    public GlobalMessageDetails(String message, String media, long timeStamp, String from) {
        super(message, media, timeStamp, from);
    }

    @Override
    public GlobalMessageDetails setFrom(String from) {
        super.setFrom(from);
        return GlobalMessageDetails.this;
    }

}
