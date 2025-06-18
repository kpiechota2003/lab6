module org.example.lab6_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;


    opens org.example.lab6_1.controller to javafx.fxml;
    exports org.example.lab6_1.controller;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens org.example.lab6_1 to javafx.fxml;
    exports org.example.lab6_1;
}