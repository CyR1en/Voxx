package com.cyr1en.voxx.client;

import com.cyr1en.voxx.client.connection.ReqResClientConnection;
import com.cyr1en.voxx.client.connection.UpdateMessageConnection;
import com.cyr1en.voxx.client.controllers.LoginController;
import com.cyr1en.voxx.commons.model.User;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class VoxxApplication extends Application {

    public static Pattern HOST_PORT_REGEX = Pattern.compile("(.*):(\\d+)");

    public static String serverHost = "localhost";
    public static int serverPort = 8008;

    private ReqResClientConnection client;
    private UpdateMessageConnection uMConnection;
    private PrimaryStageManager stageManager;
    private User assocUser;

    @Override
    public void start(Stage stage) throws IOException {
        stageManager = new PrimaryStageManager(this, stage);
        Consumer<LoginController> consumer = c -> c.setVoxxApplication(this);
        stageManager.setScene("/fxml/voxx-login.fxml", consumer);
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public PrimaryStageManager getStageManager() {
        return this.stageManager;
    }

    public ReqResClientConnection getClient() {
        return client;
    }

    public UpdateMessageConnection getUMConnection() {
        return uMConnection;
    }

    public User getAssocUser() {
        return this.assocUser;
    }

    public void setAssocUser(User user) {
        this.assocUser = user;
    }

    public void setClient(ReqResClientConnection client) {
        this.client = client;
    }

    public void setUMConnection(UpdateMessageConnection uMConnection) {
        this.uMConnection = uMConnection;
    }

    public static void changeServerAddr(String str) {
        var matcher = HOST_PORT_REGEX.matcher(str);
        if (matcher.matches()) {
            serverHost = matcher.group(1);
            serverPort = Integer.parseInt(matcher.group(2));
            System.out.println("Changed host to " + serverHost + ":" + serverPort);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}