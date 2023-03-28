package com.cyr1en.vox;

import com.cyr1en.esal.Server;
import com.cyr1en.esal.events.EventBus;
import com.cyr1en.esal.events.annotation.EventListener;
import com.cyr1en.esal.events.server.ClientConnectEvent;
import com.cyr1en.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.esal.events.server.ClientMessageEvent;

public class ConnectionListener implements EventBus.Listener {

    @EventListener
    public void onClientConnect(ClientConnectEvent event) {
        Server.LOGGER.info("[Vox] New client ({}) connected",  event.clientConnection().getRemoteAddress());
    }

    @EventListener
    public void onClientMessage(ClientMessageEvent event) {
        var msg = event.getMessage();
        var client = event.getClientConnection();
        Server.LOGGER.info("[Vox] Client said: " + msg);
        client.getOut().println("[Vox] Response");
    }

    @EventListener
    public void onClientDisconnect(ClientDisconnectEvent event) {
        Server.LOGGER.info("[Vox] Client ({}) disconnected", event.clientConnection().getRemoteAddress());
    }
}
