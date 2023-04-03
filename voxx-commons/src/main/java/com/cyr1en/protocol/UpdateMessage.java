package com.cyr1en.protocol;

import java.util.Map;

public interface UpdateMessage {
    String getIdentifier();

    Map<String, Object> getBody();
}
