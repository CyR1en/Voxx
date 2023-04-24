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

    public UpdateMessageConnection(User user, String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        socket.setKeepAlive(true);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        setConnection(user.getUsername());
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        onUpdateMessage = (s) -> {
        };
    }

    private void setConnection(String mainUser) {
        var req = new JSONObject().put("request-id", "su");
        var param = new JSONObject().put("main-user", mainUser);
        req.put("params", param);
        System.out.println("Sending: " + req);
        out.println(ProtocolUtil.flattenJSONObject(req));
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
                if (!isConnected()) continue;
                var json = new JSONObject(line);
                onUpdateMessage.accept(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
