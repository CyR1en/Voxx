package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.Server;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Set the connection to the client an update connection.
 */
public class SetUpdateConnection implements Request {
    private final VoxxServer voxxServer;
    private final VoxxServer.UserRegistry userRegistry;

    public SetUpdateConnection(VoxxServer voxxServer) {
        this.voxxServer = voxxServer;
        this.userRegistry = voxxServer.getUserRegistry();
    }

    @Override
    public void onRequest(ClientMessageEvent event) {
        var params = new JSONObject(event.getMessage()).getJSONObject("params");
        var mainUser = params.getString("main-user");
        if (!userRegistry.getUserMap().containsKey(mainUser)) return;

        var user = userRegistry.getUserMap().get(mainUser);
        if(Objects.nonNull(user)) {
            event.getClientConnection().setAssocUser(user);
            event.getClientConnection().setSupplementalConnection(true);
            Server.LOGGER.info("Client ({}) is not a supplemental connection for ({})",
                    event.getClientConnection().getRemoteAddress(), user);
            event.getClientConnection().sendMessage("{\"response-id\": 1}");
        } else {
            event.getClientConnection().sendMessage("{\"response-id\": 0}");
        }
    }
}
