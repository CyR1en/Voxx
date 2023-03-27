package com.cyr1en.esal.events.server;

import com.cyr1en.esal.ClientConnection;

public record ClientDisconnectEvent(ClientConnection clientConnection) {
}
