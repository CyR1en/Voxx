package com.cyr1en.voxx.client.connection;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

public class UpdateMessageConnection implements Runnable {
    private final Socket socket;
    private final BufferedReader reader;
    private Consumer<JSONObject> onUpdateMessage;

    public UpdateMessageConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        onUpdateMessage = (s) -> {};
    }

    public BufferedReader getReader() {
        return reader;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void onUpdateMessage(Consumer<JSONObject> onUpdateMessage) {
        this.onUpdateMessage = onUpdateMessage;
    }

    @Override
    public void run() {
        try {
            String line = null;
            System.out.println("TASK CALL");
            while ((line = getReader().readLine()) != null) {
                if (!isConnected()) {
                    System.out.println("I'm here!");
                    continue;
                }
                var json = new JSONObject(line);
                onUpdateMessage.accept(json);
            }
            System.out.println("Out of loop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
