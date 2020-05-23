package org.shardav.server.comms.voice;

import org.shardav.server.comms.Details;
import org.shardav.server.comms.Request;

public class VoiceCall<T extends Details> extends Request<T> {

    public VoiceCall(RequestType type, T details) {
        super(type, details);
    }

    @Override
    public T getDetails() {
        return this.details;
    }

    public enum VoiceEvents {
        accept,
        call,
        offline,
        reject,
        request,
        time
    }
}
