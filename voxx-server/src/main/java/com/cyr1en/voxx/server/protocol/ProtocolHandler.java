package com.cyr1en.voxx.server.protocol;

import com.cyr1en.voxx.commons.esal.Server;
import com.cyr1en.voxx.commons.esal.events.server.ClientConnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;
import com.cyr1en.voxx.server.protocol.requests.RequestEnum;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class ProtocolHandler {

    private VoxxServer serverInstance;

    public ProtocolHandler(VoxxServer serverInstance) {
        this.serverInstance = serverInstance;
        registerProtocols();
    }

    private void registerProtocols() {

    }

    public void handOnMessage(ClientMessageEvent event) {
        var req = RequestParser.parse(event, serverInstance);
        if (Objects.nonNull(req))
            req.onRequest(event);
    }

    public void handleOnConnect(ClientConnectEvent event) {

    }

    public void handleOnDisconnect(ClientDisconnectEvent event) {

    }

    public static class RequestParser {

        public static Request parse(ClientMessageEvent event, VoxxServer server) {
            JSONObject json;
            try {
                json = new JSONObject(event.getMessage());
            } catch (JSONException e) {
                Server.LOGGER.error("Unable to parse message json from message");
                return null;
            }
            var reqID = json.getString("request-id");
            var optional = Arrays.stream(RequestEnum.values()).filter(e -> e.asString().equals(reqID)).findAny();
            if (optional.isEmpty()) return null;
            try {
                var req = optional.get();
                return req.construct(server);
            } catch (Exception e) {
                Server.LOGGER.error("Unable to parse message json from message");
            }
            return null;
        }

    }
}
