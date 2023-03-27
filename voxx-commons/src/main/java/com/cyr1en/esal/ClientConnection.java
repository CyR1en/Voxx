package com.cyr1en.esal;

import com.cyr1en.esal.events.EventBus;
import com.cyr1en.esal.events.server.ClientDisconnectEvent;
import com.cyr1en.esal.events.server.ClientMessageEvent;

import java.io.*;
import java.net.Socket;

public class ClientConnection implements Runnable {

    private final EventBus eventBus;
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private boolean isRunning;


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

    public void close() {
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
                eventBus.post(new ClientDisconnectEvent(this));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        if (isRunning) {
            System.err.println("This client runnable is already running!");
            return;
        }

        isRunning = true;
        while (!clientSocket.isClosed() && isRunning) {
            try {
                String inLine;
                while ((inLine = in.readLine()) != null)
                    eventBus.post(new ClientMessageEvent(this, inLine));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
