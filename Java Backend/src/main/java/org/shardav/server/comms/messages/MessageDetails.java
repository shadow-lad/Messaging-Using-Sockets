package org.shardav.server.comms.messages;

public class MessageDetails {

    private String id;
    private String message;
    private String from;
    private String to;
    private String media;
    private long timeStamp;

    public MessageDetails(String id, String message, long timeStamp) {
        this.id = id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.media = null;
    }

    public MessageDetails(String id, String message, String media, long timeStamp) {
        this(id,message,timeStamp);
        this.media = media;
    }



    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getMedia() {
        return media;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
