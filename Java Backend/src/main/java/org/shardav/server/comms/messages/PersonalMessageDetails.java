package org.shardav.server.comms.messages;

public class PersonalMessageDetails extends MessageDetails {

    private String to;

    /**
     * Private message with media
     *
     * @param id id of the message, it should be unique for future purposes
     * @param message the message to be sent
     * @param media the media attached to the message
     * @param timeStamp the time at which the message was sent
     * @param sender the sender of the message
     * @param to the recipient of the message
     */
    public PersonalMessageDetails(String id, String message, String media, long timeStamp, String sender, String to) {
        super(id, message, media, timeStamp, sender);
        this.to = to;
    }

    private void setTo(String to) {
        this.to = to;
    }

    /**
     * Fetch the recipient of the message
     * @return Returns the recipient of the current message.
     */
    public String getTo() {
        return this.to;
    }

    @Override
    public PersonalMessageDetails setFrom(String from) {
        super.setFrom(from);
        return PersonalMessageDetails.this;
    }

    @Override
    public PersonalMessageDetails setId(String id) {
        super.setId(id);
        return PersonalMessageDetails.this;
    }
}
