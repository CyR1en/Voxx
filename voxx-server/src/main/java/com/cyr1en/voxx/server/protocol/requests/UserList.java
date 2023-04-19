package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;

public class UserList implements Request {

    private final VoxxServer voxxServer;
    private final VoxxServer.UserRegistry userRegistry;

    public UserList(VoxxServer voxxServer) {
        this.voxxServer = voxxServer;
        this.userRegistry = voxxServer.getUserRegistry();
    }

    @Override
    public void onRequest(ClientMessageEvent event) {

    }
}
