package com.cyr1en.voxx.client.connection;

import com.cyr1en.voxx.commons.model.User;
import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class UpdateMessageConnection implements Runnable {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter out;
    private Consumer<JSONObject> onUpdateMessage;
    private boolean isRunning;

    public UpdateMessageConnection(User user, String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        socket.setKeepAlive(true);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        setConnection(user.getUsername());
        onUpdateMessage = (s) -> {
        };
        isRunning = true;
    }

    private void setConnection(String mainUser) {
        var req = new JSONObject().put("request-id", "su");
        var param = new JSONObject().put("main-user", mainUser);
        req.put("params", param);
        System.out.println("Sending: " + req);
        out.println(ProtocolUtil.flattenJSONObject(req));
        try {
            // Just flush whatever the response is for now
            reader.readLine();
        } catch (IOException ignore) {
        }
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

    public void closeConnection() {
        System.out.println("Closing UM Connection");
        isRunning = false;
        try {
            this.out.close();
            this.reader.close();
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Could not properly close connection!");
        }
    }

    @Override
    public void run() {
        try {
            String line = null;
            System.out.println("TASK CALL");
            while ((line = getReader().readLine()) != null && isRunning) {
                if (!isConnected()) continue;
                var json = new JSONObject(line);
                onUpdateMessage.accept(json);
            }
        } catch (IOException ignore) {
        }
    }
}
