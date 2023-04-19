package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.Server;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONObject;

import java.util.Objects;

public class RegisterUser implements Request {

    private static final String INCORRECT_ARG;
    private static final String RESPONSE;

    private final String reqID;
    private final VoxxServer voxx;

    static {
        INCORRECT_ARG = """
                {
                  "response-id": 0,
                  "body": {
                    "message": "%s is already taken"
                  }
                }
                """;
        RESPONSE = """
                {
                  %s,
                  "body": {
                    "user": {
                      "uid": %d,
                      "uname": "%s"
                    }
                  }
                }
                """;
    }

    public RegisterUser(VoxxServer voxx) {
        this.reqID = "ru";
        this.voxx = voxx;
    }

    @Override
    public void onRequest(ClientMessageEvent event) {
        var userRegistry = voxx.getUserRegistry();
        var json = new JSONObject(event.getMessage());
        var uname = json.getJSONObject("params").getString("uname");
        var remoteAddr = event.getClientConnection().getRemoteAddress();

        if (userRegistry.isRegistered(uname)) {
            var cc = event.getClientConnection();
            cc.sendMessage(String.format(INCORRECT_ARG, uname));
            Server.LOGGER.warn("[Vox] Client ({}) attempted to register with existing username.",
                    remoteAddr);
        } else {
            var user = userRegistry.registerNewUser(uname);
            if (Objects.isNull(user)) {
                Server.LOGGER.error("[Vox] Error registering user: {} for Client ({})", uname, remoteAddr);
                return;
            }
            Server.LOGGER.info("[Vox] Client ({}) registered as user: {}", remoteAddr, uname);
            event.getClientConnection().sendMessage(String.format(RESPONSE, "\"response-id\": 1",
                    user.getUid().asLong(), user.getUsername()));
        }
    }

}
