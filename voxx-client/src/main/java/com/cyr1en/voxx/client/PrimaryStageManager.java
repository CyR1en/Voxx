package com.cyr1en.voxx.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class PrimaryStageManager {

    private final Stage primaryStage;
    private final VoxxApplication instance;

    public PrimaryStageManager(VoxxApplication instance, Stage stage) {
        this.primaryStage = stage;
        this.instance = instance;
        primaryStage.setTitle("Voxx");

        primaryStage.setOnCloseRequest(e -> {
            if (Objects.nonNull(this.instance.getClient()))
                this.instance.getClient().closeConnection();
            if (Objects.nonNull(this.instance.getUMConnection()))
                this.instance.getUMConnection().closeConnection();
            Platform.exit();
            System.exit(0);
        });
    }

    @SuppressWarnings("unchecked")
    public <T> void setScene(String fxmlFile, Consumer<T> controllerConsumer) throws IOException {
        primaryStage.hide();
        primaryStage.setResizable(true);
        var fxmlLoader = new FXMLLoader(VoxxApplication.class.getResource(fxmlFile));
        var scene = new Scene(fxmlLoader.load());
        var controller = (T) fxmlLoader.getController();
        controllerConsumer.accept(controller);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
