package com.cyr1en.voxx.client.connection;

import javafx.concurrent.Task;

import java.io.IOException;
import java.util.Objects;

import static com.cyr1en.voxx.client.VoxxApplication.SERVER_HOST;
import static com.cyr1en.voxx.client.VoxxApplication.SERVER_PORT;

public class ConnectionTask extends Task<ReqResClientConnection> {

    private final int interval;
    private final int tries;

    public ConnectionTask(int interval, int tries) {
        this.interval = interval;
        this.tries = tries;
    }

    public ConnectionTask() {
        this(2000, 10);
    }

    @Override
    protected ReqResClientConnection call() {
        var curr_tries = 0;
        ReqResClientConnection client = null;
        var last = System.currentTimeMillis();
        while (Objects.isNull(client) && curr_tries < tries) {
            if ((System.currentTimeMillis() - last) < interval) continue;
            System.out.println("Trying to connect to server...");
            try {
                client = new ReqResClientConnection(SERVER_HOST, SERVER_PORT);
            } catch (IOException e) {
                System.err.println("Could not connect to server!");
            }
            curr_tries = curr_tries + 1;
            System.out.printf("Tries: %d%n", curr_tries);
            last = System.currentTimeMillis();
        }
        return client;
    }

}
