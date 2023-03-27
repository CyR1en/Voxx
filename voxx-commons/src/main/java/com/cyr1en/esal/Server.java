package com.cyr1en.esal;

import com.cyr1en.esal.events.EventBus;
import com.cyr1en.esal.events.server.ClientConnectEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
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

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        var executors = new ArrayList<ExecutorService>();
        try {
            serverSocket = new ServerSocket(port, backlog);
            serverSocket.setReuseAddress(true);

            while (true) {
                var clientSocket = serverSocket.accept();

                var executorService = Executors.newSingleThreadExecutor();
                var clientConnection = new ClientConnection(clientSocket, eventBus);
                clientConnections.add(clientConnection);
                executorService.execute(clientConnection);
                eventBus.post(new ClientConnectEvent(clientConnection));
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