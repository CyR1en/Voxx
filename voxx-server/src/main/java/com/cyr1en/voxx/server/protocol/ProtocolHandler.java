package com.cyr1en.voxx.server.protocol;

import com.cyr1en.voxx.commons.esal.ClientConnection;
import com.cyr1en.voxx.commons.esal.events.server.ClientConnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.commons.protocol.Response;
import com.cyr1en.voxx.commons.protocol.UpdateMessage;
import com.cyr1en.voxx.server.VoxxServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolHandler {

    private final Map<String, Request> requestMap;
    private final Map<String, Response> responseMap;
    private final Map<String, UpdateMessage> updateMessageMap;

    private VoxxServer serverInstance;

    public ProtocolHandler(VoxxServer serverInstance) {
        this.requestMap = new ConcurrentHashMap<>();
        this.responseMap = new ConcurrentHashMap<>();
        this.updateMessageMap = new ConcurrentHashMap<>();
        this.serverInstance = serverInstance;
        registerProtocols();
    }

    private void registerProtocols() {

    }

    public void handOnMessage(ClientMessageEvent event) {

    }

    public void handleOnConnect(ClientConnectEvent event) {

    }

    public void handleOnDisconnect(ClientDisconnectEvent event) {

    }

}
