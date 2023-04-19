package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;

public class SendMessage implements Request {

    private final VoxxServer server;

    public SendMessage(VoxxServer voxx) {
        this.server = voxx;
    }

    @Override
    public void onRequest(ClientMessageEvent event) {

    }

}
