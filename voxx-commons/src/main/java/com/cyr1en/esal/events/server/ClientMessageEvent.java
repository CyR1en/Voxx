package com.cyr1en.esal.events.server;

import com.cyr1en.esal.ClientConnection;

/**
 * Event that's called whenever a client sends a message to the server.
 */
public class ClientMessageEvent {

    private ClientConnection clientConnection;
    private String message;

    public ClientMessageEvent(ClientConnection clientConnection, String message) {
        this.clientConnection = clientConnection;
        this.message = message;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
