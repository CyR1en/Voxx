package com.cyr1en.voxx.commons.esal;

import com.cyr1en.voxx.commons.esal.events.EventBus;
import com.cyr1en.voxx.commons.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;
import com.cyr1en.voxx.commons.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection implements Runnable {

    private final EventBus eventBus;
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private boolean isRunning;
    private User assocUser;
    private boolean isSupplementalConnection;

    private final Server serverInstance;
    private final String remoteAddress;

    public ClientConnection(Socket clientSocket, Server serverInstance) {
        this.clientSocket = clientSocket;
        this.eventBus = serverInstance.getEventBus();
        this.serverInstance = serverInstance;
        isSupplementalConnection = false;
        try {
            this.clientSocket.setSoTimeout(0);
            this.clientSocket.setKeepAlive(true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isRunning = false;
        this.remoteAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
    }

    public void setAssocUser(@NotNull User user) {
        this.assocUser = user;
    }

    @Nullable
    public User getAssocUser() {
        return assocUser;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isConnected() {
        return clientSocket.isConnected();
    }

    public void setSupplementalConnection(boolean b) {
        this.isSupplementalConnection = b;
    }

    public boolean isSupplementalConnection() {
        return isSupplementalConnection;
    }

    public void sendMessage(String message) {
        var cleaned = message.replaceAll("\\s{2,}|\\n", "");
        out.println(cleaned);
    }

    public void close() {
        try {
            if (!clientSocket.isClosed()) {
                in.close();
                out.close();
                clientSocket.close();
                serverInstance.getClientConnections().remove(this);
                eventBus.post(new ClientDisconnectEvent(this));
                isRunning = false;
            }
        } catch (IOException e) {
            Server.LOGGER.error("Could not properly close connection! " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (isRunning) {
            Server.LOGGER.warn("This client runnable is already running!");
            return;
        }

        isRunning = true;

        try {
            String inLine;
            while ((inLine = in.readLine()) != null && isConnected())
                eventBus.post(new ClientMessageEvent(this, inLine));
            close();
        } catch (IOException e) {
            Server.LOGGER.error(e.getMessage());
            close();
        }

    }
}
