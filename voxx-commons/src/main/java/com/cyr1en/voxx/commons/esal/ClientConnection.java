package com.cyr1en.voxx.commons.esal;

import com.cyr1en.voxx.commons.esal.events.EventBus;
import com.cyr1en.voxx.commons.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.voxx.commons.esal.events.server.ClientMessageEvent;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientConnection implements Runnable {

    private final EventBus eventBus;
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private boolean isRunning;

    private final String remoteAddress;

    public ClientConnection(Socket clientSocket, EventBus eventBus) {
        this.clientSocket = clientSocket;
        this.eventBus = eventBus;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isRunning = false;
        this.remoteAddress = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
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

    public void sendMessage(String message) {
        var cleaned = message.replaceAll("[\\n\\s+]", "");
        out.println(cleaned);
    }

    public void close() {
        try {
            if (!clientSocket.isClosed()) {
                in.close();
                out.close();
                clientSocket.close();
                eventBus.post(new ClientDisconnectEvent(this));
                isRunning = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            while ((inLine = in.readLine()) != null) eventBus.post(new ClientMessageEvent(this, inLine));
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
