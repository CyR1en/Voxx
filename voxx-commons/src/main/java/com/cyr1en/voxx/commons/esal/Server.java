package com.cyr1en.voxx.commons.esal;

import com.cyr1en.voxx.commons.esal.events.EventBus;
import com.cyr1en.voxx.commons.esal.events.server.ClientConnectEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    public static final Logger LOGGER = LogManager.getLogger(Server.class);

    private final int port;
    private final int backlog;
    private final EventBus eventBus;
    private final ArrayList<ClientConnection> clientConnections;

    public Server(int port, int backlog) {
        this.port = port;
        this.backlog = backlog; //Incoming Connection Queue size
        this.eventBus = new EventBus();
        clientConnections = new ArrayList<>();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ArrayList<ClientConnection> getClientConnections() {
        return clientConnections;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        var executors = new ArrayList<ExecutorService>();
        try {
            serverSocket = new ServerSocket(port, backlog);
            serverSocket.setSoTimeout(0);

            LOGGER.info("Server abstraction layer started");

            while (true) {
                var clientSocket = serverSocket.accept();
                var executorService = Executors.newSingleThreadExecutor();
                var clientConnection = new ClientConnection(clientSocket, this);
                LOGGER.info(String.format("New client (%s)", clientConnection.getRemoteAddress()));
                eventBus.post(new ClientConnectEvent(clientConnection), () -> clientConnections.add(clientConnection));
                executorService.execute(clientConnection);
                executors.add(executorService);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(serverSocket)) {
                try {
                    clientConnections.forEach(ClientConnection::close);
                    serverSocket.close();
                } catch (IOException ignore) {
                }
            }
            executors.forEach(ExecutorService::shutdown);
        }
    }
}