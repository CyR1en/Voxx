package com.cyr1en.voxx.commons.protocol;

import java.util.Map;

public interface Response {
    int getResponseID();
    Map<String, Object> getBody();
}
