package org.shardav.server.comms.messages;

import org.shardav.server.comms.Response;

public class MessageResponse extends Response<Void> {

    String id;

    public MessageResponse(ResponseEvent status, String id) {
        super(status, ResponseType.message);
        this.id = id;
    }
}
