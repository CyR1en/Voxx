package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.model.Message;
import com.cyr1en.voxx.commons.model.UID;
import com.cyr1en.voxx.commons.model.User;
import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import org.json.JSONObject;

public class SendMessage implements Request {

    private final VoxxServer server;

    public SendMessage(VoxxServer voxx) {
        this.server = voxx;
    }

    @Override
    public void onRequest(ClientMessageEvent event) {
        var assocUser = event.getClientConnection().getAssocUser();
        VoxxServer.LOGGER.info("Client associated with: " + assocUser);
        var reqJson = new JSONObject(event.getMessage()).getJSONObject("params");
        var message = new Message(assocUser, reqJson.getString("message"), UID.Generator.generate());

        var resJson = new JSONObject();
        resJson.put("response-id", 1);
        resJson.put("body", new JSONObject().put("message", new JSONObject()
                .put("uid", message.getUID().asLong())
                .put("content", message.getContent())));
        event.getClientConnection().sendMessage(ProtocolUtil.flattenJSONObject(resJson));

        var messageJson = new JSONObject();
        messageJson.put("update-message", "nm");
        var body = new JSONObject();
        body.put("sender", new JSONObject()
                .put("uid", assocUser.getUID().asLong())
                .put("uname", assocUser.getUsername()));
        body.put("message", new JSONObject()
                .put("uid", message.getUID().asLong())
                .put("content", message.getContent()));
        messageJson.put("body", body);

        server.broadcastWithExclusion(assocUser, messageJson);
    }

}
