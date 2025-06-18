package org.example.lab6_1.utils;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {
    public enum Type { SUCCESS, ERROR, WARNING }

    public static void makeText(Stage stage, String message, int duration, Type type) {
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        Label label = new Label(message);
        label.setStyle(switch (type) {
            case SUCCESS -> "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px;";
            case ERROR -> "-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10px;";
            case WARNING -> "-fx-background-color: #ff9800; -fx-text-fill: black; -fx-padding: 10px;";
        });

        StackPane pane = new StackPane(label);
        pane.setStyle("-fx-background-radius: 5; -fx-effect: dropshadow(gaussian, black, 5, 0, 0, 1);");
        popup.getContent().add(pane);

        Scene scene = stage.getScene();
        popup.show(stage, scene.getWindow().getX() + scene.getWidth()/2 - 100, scene.getWindow().getY() + 50);

        FadeTransition fade = new FadeTransition(Duration.millis(duration), pane);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> popup.hide());
        fade.play();
    }
}
