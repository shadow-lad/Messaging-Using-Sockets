package org.shardav.server.comms.messages;

import org.shardav.server.comms.Details;

public class MessageDetails implements Details {

    private final String message;
    private final long time;
    private String media;
    private String from;

    /**
     * Message without any media
     *
     * @param message the body of the message to be sent
     * @param time    the time at which the message was sent
     * @param from    the person who sent the message
     */
    protected MessageDetails(String message, long time, String from) {
        this.message = message;
        this.time = time;
        this.media = null;
        this.from = from;
    }

    /**
     * Message with media
     *
     * @param message the body of the message to be sent
     * @param media   the media of the message
     * @param time    the time at which the message was sent
     * @param from    the person who sent the message
     */
    protected MessageDetails(String message, String media, long time, String from) {
        this(message, time, from);
        this.media = media;
    }

    /**
     * Fetch the body of the current message
     *
     * @return Returns the body of the current message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Fetch the url of the media of the message
     *
     * @return Returns the url of the media of the current message.
     */
    public String getMedia() {
        return media;
    }

    /**
     * Fetch the sender of the message
     *
     * @return Returns the sender of the current message.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Set the sender username
     *
     * @param from username of the sender
     */
    public MessageDetails setFrom(String from) {
        this.from = from;
        return MessageDetails.this;
    }

    /**
     * Fetch the timestamp of the message
     *
     * @return Returns the timestamp of the current message.
     */
    public long getTime() {
        return time;
    }

}
