package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.esal.ClientConnection;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;

import java.util.Map;

public class RegisterUser implements Request {
    private final String reqID;
    private final VoxxServer voxx;


    public RegisterUser(VoxxServer voxx) {
        this.reqID = "ru";
        this.voxx = voxx;
    }

    public void onRequest(ClientMessageEvent event) {
        //var registered = voxx.getUserRegistry().registerNewUser();
    }

    @Override
    public String getRequestID() {
        return this.reqID;
    }

    @Override
    public Map<String, Object> getParams() {
        return null;
    }
}
