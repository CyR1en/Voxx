package com.cyr1en.vox;

import com.cyr1en.esal.Server;
import com.cyr1en.esal.events.EventBus;


public class VoxxServer {

    private final Server server;
    private final EventBus serverEventBus;

    public VoxxServer() {
        server = new Server(1010, 500);
        this.serverEventBus = server.getEventBus();
        registerListeners();
        server.run();
    }

    private void registerListeners() {
        this.serverEventBus.subscribeListeners(new ConnectionListener());
    }

    public static void main(String[] args) {
        new VoxxServer();
    }

}
