package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserList implements Request {

    private final VoxxServer voxxServer;
    private final VoxxServer.UserRegistry userRegistry;

    public UserList(VoxxServer voxxServer) {
        this.voxxServer = voxxServer;
        this.userRegistry = voxxServer.getUserRegistry();
    }

    @Override
    public void onRequest(ClientMessageEvent event) {
        var response = new JSONObject();
        var users = new JSONArray();
        userRegistry.getUserMap().forEach((k, v) -> users.put(new JSONObject()
                .put("uid", v.getUID().asLong())
                .put("uname", v.getUsername())));
        response.put("response-id", 1);
        response.put("body", new JSONObject()
                .put("users", users));
        event.getClientConnection().sendMessage(ProtocolUtil.flattenJSONObject(response));
    }
}
