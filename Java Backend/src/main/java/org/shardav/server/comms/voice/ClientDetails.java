package org.shardav.server.comms.voice;

import org.shardav.server.comms.Details;

public class ClientDetails implements Details {

    final private String ip;
    final private int port;

    public ClientDetails(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
