package com.cyr1en.protocol;

import java.util.Map;

public interface Request {

    String getRequestID();

    Map<String, Object> getParams();
}
