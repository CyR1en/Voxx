package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONObject;

public class Ping implements Request {
    private final VoxxServer server;

    Ping(VoxxServer voxx) {
        this.server = voxx;
    }

    @Override
    public void onRequest(ClientMessageEvent event) {
        var ts = System.currentTimeMillis();
        var res = new JSONObject().put("response-id", 1).put("body", ts);
        event.getClientConnection().sendMessage(ProtocolUtil.flattenJSONObject(res));
    }
}
