module com.cyr1en.voxx.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.json;
    requires Project.Voxx.voxx.commons.main;

    opens com.cyr1en.voxx.client to javafx.fxml;
    exports com.cyr1en.voxx.client;
    exports com.cyr1en.voxx.client.controllers;
    exports com.cyr1en.voxx.client.connection;
    opens com.cyr1en.voxx.client.controllers to javafx.fxml;
}