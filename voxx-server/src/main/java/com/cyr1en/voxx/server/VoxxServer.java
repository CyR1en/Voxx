package com.cyr1en.voxx.server;

import com.cyr1en.voxx.commons.esal.ClientConnection;
import com.cyr1en.voxx.commons.esal.Server;
import com.cyr1en.voxx.commons.esal.events.EventBus;
import com.cyr1en.voxx.commons.esal.events.annotation.EventListener;
import com.cyr1en.voxx.commons.esal.events.server.ClientConnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.model.UID;
import com.cyr1en.voxx.commons.model.User;
import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import com.cyr1en.voxx.server.protocol.ProtocolHandler;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class VoxxServer extends Server implements EventBus.Listener {

    private final UserRegistry userRegistry;
    private final ProtocolHandler protocolHandler;

    public VoxxServer() {
        super(8008, 500);
        getEventBus().setExecutorServiceSupplier(Executors::newCachedThreadPool);
        getEventBus().subscribeListeners(this);
        this.userRegistry = new UserRegistry();
        this.protocolHandler = new ProtocolHandler(this);
        run();
    }

    public synchronized void broadcastWithExclusions(User excludedUser, JSONObject object) {
        for (ClientConnection cc : getClientConnections()) {
            if (cc.getAssocUser() == null) continue;
            if (!cc.getAssocUser().equals(excludedUser) && cc.isSupplementalConnection()) {
                LOGGER.info("Broadcasting to: " + cc.getRemoteAddress());
                cc.sendMessage(ProtocolUtil.flattenJSONObject(object));
            }
        }
    }

    @EventListener
    public void onClientConnect(ClientConnectEvent event) {
        Server.LOGGER.info("[Vox] New client ({}) connected", event.clientConnection().getRemoteAddress());
    }

    @EventListener
    public void onClientMessage(ClientMessageEvent event) {
        // Ignore if this is a supplemental connection
        if (event.getClientConnection().isSupplementalConnection()) return;

        var msg = event.getMessage();
        Server.LOGGER.info("[Vox] Client said: " + msg);
        protocolHandler.handOnMessage(event);
    }

    @EventListener
    public void onClientDisconnect(ClientDisconnectEvent event) {
        Server.LOGGER.info("[Vox] Client ({}) disconnected {}", event.clientConnection().getRemoteAddress(),
                event.clientConnection().isSupplementalConnection() ? "[S]" : "");
        if (event.clientConnection().getAssocUser() == null) return;
        var user = event.clientConnection().getAssocUser();
        userRegistry.userMap.remove(user.getUsername());
        var responseJson = new JSONObject();
        responseJson.put("update-message", "ud");
        var body = new JSONObject().put("user", new JSONObject().put("uid", user.getUID().asLong())
                .put("uname", user.getUsername()));
        responseJson.put("body", body);
        broadcastWithExclusions(user, responseJson);
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
