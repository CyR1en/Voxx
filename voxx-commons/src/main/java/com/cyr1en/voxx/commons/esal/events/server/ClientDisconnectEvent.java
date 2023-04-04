package com.cyr1en.voxx.commons.esal.events.server;

import com.cyr1en.voxx.commons.esal.ClientConnection;

public record ClientDisconnectEvent(ClientConnection clientConnection) {
}
