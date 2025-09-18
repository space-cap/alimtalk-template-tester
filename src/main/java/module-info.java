module javafx.hello.world {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    exports com.ezlevup;
    exports com.ezlevup.controller;
    exports com.ezlevup.util;

    opens com.ezlevup.controller to javafx.fxml;
}