package com.cyr1en.voxx.commons.protocol;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;

public interface Request {

    void onRequest(ClientMessageEvent event);
}
