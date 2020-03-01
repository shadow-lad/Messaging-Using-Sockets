package org.shardav.server.comms;

import java.util.Map;

public interface Details {

    /**
     * Converts the attributes of the current object to a map and returns it.
     *
     * @return A map containing the details of the attributes of the object.
     */
    Map<String, Object> toMap();

}
