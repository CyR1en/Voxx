package com.cyr1en.voxx.server.protocol.requests;

import com.cyr1en.voxx.commons.protocol.Request;
import com.cyr1en.voxx.server.VoxxServer;

import java.lang.reflect.InvocationTargetException;

public enum RequestEnum {
    REGISTER_USER("ru", RegisterUser.class),
    SEND_MESSAGE("sm", SendMessage.class),
    USER_LIST("ul", UserList.class),
    SET_UPDATE_CONNECTION("su", SetUpdateConnection.class);

    private final String id;
    private final Class<? extends Request> reqClass;

    RequestEnum(String id, Class<? extends Request> reqClass) {
        this.id = id;
        this.reqClass = reqClass;
    }

    public String asString() {
        return id;
    }

    public Request construct(VoxxServer serverInstance) throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        var constructor = reqClass.getConstructor(VoxxServer.class);
        return constructor.newInstance(serverInstance);
    }
}
