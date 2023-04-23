package com.cyr1en.voxx.client;

import com.cyr1en.voxx.client.connection.ReqResClientConnection;
import com.cyr1en.voxx.client.controllers.LoginController;
import com.cyr1en.voxx.commons.model.User;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class VoxxApplication extends Application {

    public static String SERVER_HOST = "localhost";
    public static int SERVER_PORT = 8008;
    public static Pattern HOST_PORT_REGEX = Pattern.compile("(.*):(\\d+)");

    private ReqResClientConnection client;
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

    public User getAssocUser() {
        return this.assocUser;
    }

    public void setAssocUser(User user) {
        this.assocUser = user;
    }

    public void setClient(ReqResClientConnection client) {
        this.client = client;
    }

    public static void changeHost(String str) {
        var matcher = HOST_PORT_REGEX.matcher(str);
        if (matcher.matches()) {
            SERVER_HOST = matcher.group(1);
            SERVER_PORT = Integer.parseInt(matcher.group(2));
            System.out.println("Changed host to " + SERVER_HOST + ":" + SERVER_PORT);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class ConnectionTask extends Task<ReqResClientConnection> {

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
}