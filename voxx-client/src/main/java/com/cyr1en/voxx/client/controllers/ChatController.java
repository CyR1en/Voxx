package com.cyr1en.voxx.client.controllers;

import com.cyr1en.voxx.client.VoxxApplication;
import com.cyr1en.voxx.client.connection.ReqResClientConnection;
import com.cyr1en.voxx.client.connection.UpdateMessageConnection;
import com.cyr1en.voxx.commons.model.Message;
import com.cyr1en.voxx.commons.model.UID;
import com.cyr1en.voxx.commons.model.User;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;

public class ChatController {

    @FXML
    private Label unameLabel;

    @FXML
    private VBox chatBox;

    @FXML
    private TextField msgField;

    @FXML
    private ListView<String> userList;

    @FXML
    private ScrollPane cBoxScrollPane;

    private VoxxApplication instance;
    private final User system;
    private UpdateMessageConnection updateMessageConnection;

    public ChatController() throws IOException {
        this.userList = new ListView<>();
        this.chatBox = new VBox();
        this.system = new User(UID.Generator.generate(), "System");
        this.unameLabel = new Label();
        this.cBoxScrollPane = new ScrollPane();
    }

    public void initialize() {
        System.out.println("ChatController initialized");
        chatBox.getChildren().clear();
        chatBox.heightProperty().addListener((observable, oldValue, newValue) -> cBoxScrollPane.setVvalue(cBoxScrollPane.getVmax()));
        addMessage(new Message(system,
                "Welcome to Voxx! This chat is not moderated, please be nice and civil.", UID.Generator.generate()));
    }

    public void updateUserList() {
        if (!instance.isConnected()) return;
        var response = instance.getClient().sendRequest(new JSONObject().put("request-id", "ul"));
        var body = response.getJSONObject("body");
        body.getJSONArray("users").forEach(o -> {
            var jO = (JSONObject) o;
            addUserList(jO.getString("uname"));
        });
    }

    private void connectSupplemental() {
        try {
            this.updateMessageConnection = new UpdateMessageConnection(instance.getAssocUser(),
                    VoxxApplication.SERVER_HOST, VoxxApplication.SERVER_PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTask() {
        connectSupplemental();
        updateMessageConnection.onUpdateMessage(msg -> {
            System.out.println("Update message: " + msg);
            var key = msg.getString("update-message");
            var body = msg.getJSONObject("body");
            switch (key) {
                case "nu" -> {
                    System.out.println("Adding user");
                    var name = body.getJSONObject("user").getString("uname");
                    Platform.runLater(() -> addUserList(name));
                }
                case "nm" -> {
                    System.out.println("Adding message");
                    var senderJson = body.getJSONObject("sender");
                    var messageJson = body.getJSONObject("message");
                    var sender = new User(UID.of(senderJson.getLong("uid")), senderJson.getString("uname"));
                    var message = new Message(sender, messageJson.getString("content"),
                            UID.of(messageJson.getLong("uid")));
                    Platform.runLater(() -> addMessage(message));
                }
                case "ud" -> {
                    var name = body.getJSONObject("user").getString("uname");
                    System.out.println("Removing user " + name);
                    Platform.runLater(() -> removeUserList(name));
                }
            }
        });
        Executors.newSingleThreadExecutor().execute(updateMessageConnection);
    }

    public void addMessage(Message message) {
        var vbox = new VBox();
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setPadding(new Insets(10, 0, 0, 0));

        var infoHbox = new HBox();
        infoHbox.setAlignment(Pos.CENTER_LEFT);

        var uNameLabel = new Label(message.getSender().getUsername());
        uNameLabel.setFont(new Font(15));
        uNameLabel.setTextFill(Color.BLACK);
        uNameLabel.setPadding(new Insets(0, 5, 0, 0));

        var tsLabel = new Label(message.getUID().getTimestampString());
        tsLabel.setFont(new Font(10));
        tsLabel.setTextFill(Color.valueOf("#949494"));
        infoHbox.getChildren().addAll(uNameLabel, tsLabel);

        var textArea = new TextArea(message.getContent());
        textArea.getStylesheets().add("/css/chatbox.css");
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight((double) textArea.getText().length() / 30 * 21);

        vbox.getChildren().addAll(infoHbox, textArea);
        chatBox.getChildren().add(vbox);
    }

    @FXML
    public void onKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            var text = msgField.getText();
            var reqJson = new JSONObject();
            reqJson.put("request-id", "sm");
            reqJson.put("params", new JSONObject().put("message", text));

            var response = instance.getClient().sendRequest(reqJson);
            var resMessage = response.getJSONObject("body").getJSONObject("message");
            var message = new Message(this.instance.getAssocUser(), resMessage.getString("content"),
                    UID.of(resMessage.getLong("uid")));
            addMessage(message);
            msgField.setText("");
            msgField.setPromptText("Type message here...");
        }
    }


    public void setUnameLabel(String unameLabel) {
        this.unameLabel.setText(unameLabel);
    }

    public void addUserList(String uName) {
        userList.getItems().add(uName);
    }

    public void removeUserList(String uName) {
        userList.getItems().remove(uName);
    }

    public void setVoxxApplication(VoxxApplication instance) {
        this.instance = instance;
    }

    public VoxxApplication getVoxxInstance() {
        return this.instance;
    }
}
