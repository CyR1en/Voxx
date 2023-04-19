package com.cyr1en.voxx.commons.protocol;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;

import java.util.Map;

public interface Request {

    void onRequest(ClientMessageEvent event);
}
