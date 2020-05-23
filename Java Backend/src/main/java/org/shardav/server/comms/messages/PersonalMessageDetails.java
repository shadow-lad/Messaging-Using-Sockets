package org.shardav.server.comms.messages;

public class PersonalMessageDetails extends MessageDetails {

    private String id;
    private String to;

    /**
     * Private message with media
     *
     * @param message   the message to be sent
     * @param media     the media attached to the message
     * @param timeStamp the time at which the message was sent
     * @param from      the sender of the message
     * @param to        the recipient of the message
     */
    public PersonalMessageDetails(String id, String message, String media, long timeStamp, String from, String to) {
        super(message, media, timeStamp, from);
        this.id = id;
        this.to = to;
    }

    /**
     * Fetch the recipient of the message
     *
     * @return Returns the recipient of the current message.
     */
    public String getTo() {
        return this.to;
    }

    private void setTo(String to) {
        this.to = to;
    }

    @Override
    public PersonalMessageDetails setFrom(String from) {
        super.setFrom(from);
        return PersonalMessageDetails.this;
    }

    public String getId() {
        return this.id;
    }

    public PersonalMessageDetails setId(String id) {
        this.id = id;
        return PersonalMessageDetails.this;
    }
}
