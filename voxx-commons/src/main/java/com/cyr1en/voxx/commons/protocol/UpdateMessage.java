package com.cyr1en.voxx.commons.protocol;

import java.util.Map;

public interface UpdateMessage {
    String getIdentifier();

    Map<String, Object> getBody();
}
