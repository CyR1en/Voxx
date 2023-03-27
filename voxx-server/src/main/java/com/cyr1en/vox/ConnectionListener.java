package com.cyr1en.vox;

import com.cyr1en.esal.events.EventBus;
import com.cyr1en.esal.events.annotation.EventListener;
import com.cyr1en.esal.events.server.ClientConnectEvent;
import com.cyr1en.esal.events.server.ClientDisconnectEvent;

public class ConnectionListener implements EventBus.Listener {

    @EventListener
    public void onClientConnect(ClientConnectEvent event) {
        var socket = event.clientConnection().getClientSocket();
        System.out.println("Client Socket Remote Address: " + socket.getRemoteSocketAddress().toString());
        System.out.println("Client Socket Port: " + socket.getPort());
    }


    @EventListener
    public void onClientDisconnect(ClientDisconnectEvent event) {
        var socket = event.clientConnection().getClientSocket();
        System.out.println("Client Socket Remote Address: " + socket.getRemoteSocketAddress().toString());
        System.out.println("Client Socket Port: " + socket.getPort());
    }
}
