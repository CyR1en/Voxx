package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.Server;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONObject;

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
            event.getClientConnection().setAssocUser(user);
            Server.LOGGER.info("[Vox] Client ({}) registered as user: {}", remoteAddr, uname);
            event.getClientConnection().sendMessage(String.format(RESPONSE, "\"response-id\": 1",
                    user.getUID().asLong(), user.getUsername()));

            var responseJson = new JSONObject();
            responseJson.put("update-message", "nu");
            var body = new JSONObject().put("user", new JSONObject().put("uid", user.getUID().asLong())
                    .put("uname", user.getUsername()));
            responseJson.put("body", body);
            voxx.broadcastWithExclusion(user, responseJson);
        }
    }

}
