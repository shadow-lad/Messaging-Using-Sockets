package org.shardav.server.comms.voice;

import org.shardav.server.comms.Details;
import org.shardav.server.comms.Response.ResponseType;
import org.shardav.server.comms.voice.VoiceCall.VoiceEvents;

public class VoiceResponse<T extends Details> {

    final private ResponseType type;
    private VoiceEvents event;
    private String message;
    private T details;

    public VoiceResponse(VoiceEvents event) {
        this.type = ResponseType.voice;
        this.event = event;
    }

    public VoiceResponse(VoiceEvents event, String message) {
        this(event);
        this.message = message;

    }

    public VoiceResponse(VoiceEvents event, T details) {
        this(event);
        this.details = details;
    }

    public ResponseType getType() {
        return type;
    }

    public VoiceEvents getEvent() {
        return event;
    }

    public void setEvent(VoiceEvents event) {
        this.event = event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getDetails() {
        return details;
    }

    public void setDetails(T details) {
        this.details = details;
    }
}
