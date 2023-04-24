package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONObject;

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
        event.getClientConnection().setAssocUser(user);
        event.getClientConnection().setSupplementalConnection(true);
    }
}
