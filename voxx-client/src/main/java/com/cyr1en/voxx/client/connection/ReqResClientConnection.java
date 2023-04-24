
package com.cyr1en.voxx.client.connection;

import com.cyr1en.voxx.commons.protocol.ProtocolUtil;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ReqResClientConnection {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader reader;

    public ReqResClientConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        socket.setKeepAlive(true);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void closeConnection() {
        try {
            socket.close();
            out.close();
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not properly close connection!");
        }
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public BufferedReader getReader() {
        return reader;
    }

    public synchronized JSONObject sendRequest(JSONObject req) {
        var reqStr = ProtocolUtil.flattenJSONObject(req);
        out.println(reqStr);
        try {
            return new JSONObject(reader.readLine());
        } catch (Exception e) {
            System.err.println("An error occurred sending a request");
        }
        return null;
    }

}
