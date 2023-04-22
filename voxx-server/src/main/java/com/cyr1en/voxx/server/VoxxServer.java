package com.cyr1en.voxx.server;

import com.cyr1en.voxx.commons.esal.ClientConnection;
import com.cyr1en.voxx.commons.esal.Server;
import com.cyr1en.voxx.commons.esal.events.EventBus;
import com.cyr1en.voxx.commons.esal.events.annotation.EventListener;
import com.cyr1en.voxx.commons.esal.events.server.ClientConnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.model.Message;
import com.cyr1en.voxx.commons.model.UID;
import com.cyr1en.voxx.commons.model.User;
import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import com.cyr1en.voxx.server.protocol.ProtocolHandler;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class VoxxServer extends Server implements EventBus.Listener {

    private final UserRegistry userRegistry;
    private final ProtocolHandler protocolHandler;

    public VoxxServer() {
        super(1010, 500);
        getEventBus().subscribeListeners(this);
        this.userRegistry = new UserRegistry();
        this.protocolHandler = new ProtocolHandler(this);
        run();
    }

    public synchronized void broadcastWithExclusion(User user, JSONObject object) {
        for (ClientConnection cc : getClientConnections()) {
            var userAssociated = Objects.nonNull(cc.getAssocUser());
            if (!userAssociated || user.getUID().equals(cc.getAssocUser().getUID())) continue;
            LOGGER.info("Broadcasting to: " + cc.getRemoteAddress());
            cc.sendMessage(ProtocolUtil.flattenJSONObject(object));
        }
    }

    @EventListener
    public void onClientConnect(ClientConnectEvent event) {
        Server.LOGGER.info("[Vox] New client ({}) connected", event.clientConnection().getRemoteAddress());
        protocolHandler.handleOnConnect(event);
    }

    @EventListener
    public void onClientMessage(ClientMessageEvent event) {
        var msg = event.getMessage();
        Server.LOGGER.info("[Vox] Client said: " + msg);
        protocolHandler.handOnMessage(event);
    }

    @EventListener
    public void onClientDisconnect(ClientDisconnectEvent event) {
        Server.LOGGER.info("[Vox] Client ({}) disconnected", event.clientConnection().getRemoteAddress());
        protocolHandler.handleOnDisconnect(event);
        if (event.clientConnection().getAssocUser() == null) return;
        var user = event.clientConnection().getAssocUser();
        userRegistry.userMap.remove(user.getUsername());
        var responseJson = new JSONObject();
        responseJson.put("update-message", "ud");
        var body = new JSONObject().put("user", new JSONObject().put("uid", user.getUID().asLong())
                .put("uname", user.getUsername()));
        responseJson.put("body", body);
        broadcastWithExclusion(event.clientConnection().getAssocUser(), responseJson);
    }

    public UserRegistry getUserRegistry() {
        return this.userRegistry;
    }

    public static class UserRegistry {
        /**
         * It is pretty redundant to have the username as a key for this map since User encapsulates this data type.
         * However, to mitigate {@link java.util.ConcurrentModificationException} across multiple client threads.
         * I've decided to opt with using a {@link ConcurrentHashMap} for faster user CRUD.
         */
        private ConcurrentHashMap<String, User> userMap;

        public UserRegistry() {
            userMap = new ConcurrentHashMap<>();
        }

        public synchronized User registerNewUser(String username) {
            if (isRegistered(username)) return null;
            var uid = UID.Generator.generate();
            var user = new User(uid, username);
            userMap.put(username, user);
            return user;
        }

        public synchronized boolean isRegistered(String username) {
            return userMap.containsKey(username);
        }

        public ConcurrentHashMap<String, User> getUserMap() {
            return userMap;
        }
    }

}
