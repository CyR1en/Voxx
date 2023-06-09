package com.cyr1en.voxx.client.controllers;

import com.cyr1en.voxx.client.VoxxApplication;
import com.cyr1en.voxx.client.connection.ConnectionTask;
import com.cyr1en.voxx.commons.model.UID;
import com.cyr1en.voxx.commons.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class LoginController {

    private static final Pattern UNAME_REGEX = Pattern
            .compile("^[A-Za-z][A-Za-z0-9_]{3,6}$");

    private static final Color F_RED = Color.valueOf("#ff1f1f");
    private static final Color F_GREEN = Color.valueOf("#11A60C");
    private static final Color F_ORANGE = Color.valueOf("#E67E22");

    private VoxxApplication voxxApplication;
    private boolean isAttemptingConnect;

    @FXML
    private Label warningLabel;

    @FXML
    private Button scButton;

    @FXML
    private TextField uNameField;

    @FXML
    private Circle connectionStatus;

    public LoginController() {
        warningLabel = new Label();
        connectionStatus = new Circle();
        isAttemptingConnect = false;
    }

    public void initialize() {
        warningLabel.setText("");
        Tooltip.install(connectionStatus, new Tooltip("Right click to change server, left click to try reconnecting."));
        connectToServer(true, 2);
    }

    public void setVoxxApplication(VoxxApplication voxxApplication) {
        this.voxxApplication = voxxApplication;
    }

    @FXML
    protected void onStartChattingPress() throws IOException {
        var uName = uNameField.getText();
        if (!UNAME_REGEX.matcher(uName).find()) {
            warningLabel.setText("Invalid username!");
            return;
        } else if (!voxxApplication.isConnected()) {
            warningLabel.setText("Not connected to server!");
            return;
        } else
            warningLabel.setText("");

        var reqJSON = new JSONObject();
        reqJSON.put("request-id", "ru");
        reqJSON.put("params", new JSONObject().put("uname", uName));
        var response = voxxApplication.getClient().sendRequest(reqJSON);
        System.out.println("RU response: " + response);
        var resBody = response.getJSONObject("body");
        var resId = response.getInt("response-id");
        if (resId == 0) {
            warningLabel.setText(resBody.getString("message"));
        } else if (resId == 1) {
            var userJSON = resBody.getJSONObject("user");
            var uid = UID.of(userJSON.getLong("uid"));
            voxxApplication.setAssocUser(new User(uid, userJSON.getString("uname")));

            Consumer<ChatController> consumer = (c) -> {
                c.setVoxxApplication(this.voxxApplication);
                c.setUnameLabel(this.voxxApplication.getAssocUser().getUsername());
                c.startTask();
                c.updateUserList();
            };
            this.voxxApplication.getStageManager().setScene("/fxml/voxx-chat-window.fxml", consumer);
        }
    }

    @FXML
    protected void onStatusCircleClick(MouseEvent event) {
        //check if right click
        if (this.voxxApplication.isConnected()) return;

        if (event.getButton() == MouseButton.SECONDARY)
            connectToServer(false);
        else if (event.getButton() == MouseButton.PRIMARY)
            connectToServer(true);
    }

    private void connectToServer(boolean promptServer) {
        connectToServer(promptServer, 3);
    }

    private Optional<String> promptServer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set server address");
        dialog.setHeaderText(null);
        dialog.setContentText("Host and port:");
        dialog.getEditor().setPromptText("server_address:port");
        Platform.runLater(() -> {
            dialog.getDialogPane().requestFocus();
            var stageManager = voxxApplication.getStageManager();
            var stageCenter = stageManager.getStageCenter();
            dialog.setX(stageCenter.getX() - (dialog.getWidth() / 2));
            dialog.setY(stageCenter.getY() - (dialog.getHeight() / 2));
        });
        return dialog.showAndWait();
    }

    private void connectToServer(boolean promptServer, int tries) {
        if (isAttemptingConnect) return;
        isAttemptingConnect = true;

        if (!promptServer)
            promptServer().ifPresent(VoxxApplication::changeServerAddr);

        var exec = Executors.newSingleThreadScheduledExecutor();
        var task = new ConnectionTask(2000, tries);
        task.setOnScheduled(e -> {
            if (voxxApplication.isConnected()) {
                task.cancel();
                connectionStatus.setFill(F_GREEN);
                isAttemptingConnect = false;
            }
        });
        task.setOnRunning(e -> connectionStatus.setFill(F_ORANGE));
        task.setOnSucceeded(e -> {
            var client = task.getValue();
            if (client == null) {
                connectionStatus.setFill(F_RED);
            } else {
                voxxApplication.setClient(client);
                connectionStatus.setFill(F_GREEN);
            }
            isAttemptingConnect = false;
        });
        exec.schedule(task, 0, TimeUnit.SECONDS);
        exec.shutdown();
    }

}