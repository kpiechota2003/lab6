package org.example.lab6_1.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.lab6_1.utils.Toast;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.UnaryOperator;

public class MainController {
    @FXML private ImageView logoImageView;
    @FXML private ImageView originalImageView;
    @FXML private ImageView processedImageView;
    @FXML private ComboBox<String> operationComboBox;
    @FXML private Button executeButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;

    private File loadedImageFile;
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private boolean isImageModified = false;

    @FXML
    public void initialize() {
        operationComboBox.getItems().addAll("Konwersja do czerni i bieli", "Negatyw", "Rozmycie", "Konturowanie");
        operationComboBox.setValue(null);
        executeButton.setDisable(true);
        saveButton.setDisable(true);
        resetButton.setDisable(true);
        Image logo = new Image(getClass().getResourceAsStream("/org/example/lab6_1/assets/logo.png"));
        logoImageView.setImage(logo);
    }

    @FXML
    private void handleLoadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz obraz (.jpg)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG files", "*.jpg"));

        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            try {
                if (!file.getName().toLowerCase().endsWith(".jpg")) {
                    Toast.makeText(getStage(), "Niedozwolony format pliku", 2000, Toast.Type.ERROR);
                    return;
                }
                originalImage = ImageIO.read(file);
                if (originalImage == null) throw new IOException();
                loadedImageFile = file;

                processedImage = deepCopy(originalImage);
                originalImageView.setImage(SwingFXUtils.toFXImage(originalImage, null));
                processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));

                isImageModified = false;
                executeButton.setDisable(false);
                saveButton.setDisable(false);
                resetButton.setDisable(false);
                Toast.makeText(getStage(), "Pomyślnie załadowano plik", 2000, Toast.Type.SUCCESS);
            } catch (IOException e) {
                Toast.makeText(getStage(), "Nie udało się załadować pliku", 2000, Toast.Type.ERROR);
            }
        }
    }

    @FXML
    private void handleExecuteOperation(ActionEvent event) {
        String operation = operationComboBox.getValue();
        if (operation == null || processedImage == null) {
            Toast.makeText(getStage(), "Brak obrazu lub operacji", 2000, Toast.Type.WARNING);
            return;
        }

        switch (operation) {
            case "Konwersja do czerni i bieli" -> processedImage = applyGrayscale(processedImage);
            case "Negatyw" -> processedImage = applyNegative(processedImage);
            case "Rozmycie" -> processedImage = applyBlur(processedImage);
            case "Konturowanie" -> processedImage = applyContour(processedImage);
        }

        processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
        isImageModified = true;
    }

    @FXML
    private void handleSaveImage(ActionEvent event) {
        if (processedImage == null) return;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(getStage());

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 20;");
        TextField filenameField = new TextField();
        filenameField.setPromptText("Podaj nazwę pliku");

        Text warning = new Text();
        warning.setFill(Color.ORANGE);
        if (!isImageModified) warning.setText("Na pliku nie zostały wykonane żadne operacje!");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        Button save = new Button("Zapisz");
        Button cancel = new Button("Anuluj");

        save.setOnAction(e -> {
            String name = filenameField.getText().trim();
            if (name.length() < 3 || name.length() > 100) {
                errorLabel.setText("Nazwa pliku musi mieć 3–100 znaków");
                return;
            }

            File outputFile = new File(System.getProperty("user.home") + "/Pictures/" + name + ".jpg");
            if (outputFile.exists()) {
                Toast.makeText(getStage(), "Plik już istnieje. Podaj inną nazwę!", 3000, Toast.Type.ERROR);
                dialog.close();
                return;
            }

            try {
                ImageIO.write(processedImage, "jpg", outputFile);
                Toast.makeText(getStage(), "Zapisano obraz: " + name + ".jpg", 2000, Toast.Type.SUCCESS);
            } catch (IOException ex) {
                Toast.makeText(getStage(), "Błąd zapisu pliku", 2000, Toast.Type.ERROR);
            }
            dialog.close();
        });

        cancel.setOnAction(e -> dialog.close());
        box.getChildren().addAll(new Label("Nazwa pliku:"), filenameField, warning, errorLabel, save, cancel);

        Scene dialogScene = new Scene(box);
        dialog.setScene(dialogScene);
        dialog.setTitle("Zapisz obraz");
        dialog.show();
    }

    @FXML
    private void handleResetImage(ActionEvent event) {
        if (originalImage != null) {
            processedImage = deepCopy(originalImage);
            processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
            isImageModified = false;
            Toast.makeText(getStage(), "Obraz został zresetowany do oryginału", 2000, Toast.Type.SUCCESS);
        }
    }

    @FXML
    private void handleOpenScaleDialog(ActionEvent event) {
        if (processedImage == null) {
            Toast.makeText(getStage(), "Najpierw załaduj obraz!", 2000, Toast.Type.WARNING);
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(getStage());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        TextField widthField = createNumberField();
        widthField.setPromptText("Nowa szerokość (1–3000)");
        TextField heightField = createNumberField();
        heightField.setPromptText("Nowa wysokość (1–3000)");

        Button scaleButton = new Button("Skaluj obraz");
        Button cancelButton = new Button("Anuluj");

        scaleButton.setOnAction(e -> {
            String widthText = widthField.getText().trim();
            String heightText = heightField.getText().trim();

            if (widthText.isEmpty() || heightText.isEmpty()) {
                Toast.makeText(getStage(), "Wszystkie pola są wymagane", 2000, Toast.Type.WARNING);
                return;
            }

            int newWidth = Integer.parseInt(widthText);
            int newHeight = Integer.parseInt(heightText);

            if (newWidth <= 0 || newWidth > 3000 || newHeight <= 0 || newHeight > 3000) {
                Toast.makeText(getStage(), "Wartości muszą być z zakresu 1–3000", 2000, Toast.Type.ERROR);
                return;
            }

            BufferedImage scaled = new BufferedImage(newWidth, newHeight, processedImage.getType());
            java.awt.Graphics2D g2d = scaled.createGraphics();
            g2d.drawImage(processedImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            processedImage = scaled;
            processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
            isImageModified = true;
            Toast.makeText(getStage(), "Obraz został przeskalowany", 2000, Toast.Type.SUCCESS);
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());
        layout.getChildren().addAll(new Label("Nowa szerokość:"), widthField,
                new Label("Nowa wysokość:"), heightField,
                scaleButton, cancelButton);

        dialog.setScene(new Scene(layout));
        dialog.setTitle("Skaluj obraz");
        dialog.showAndWait();
    }

    private TextField createNumberField() {
        TextField field = new TextField();
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,4}")) {
                try {
                    if (newText.isEmpty()) return change;
                    int value = Integer.parseInt(newText);
                    if (value >= 1 && value <= 3000) return change;
                } catch (NumberFormatException ignored) {}
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
        return field;
    }

    @FXML
    private void handleOpenThresholdDialog(ActionEvent event) {
        if (processedImage == null) {
            Toast.makeText(getStage(), "Najpierw załaduj obraz!", 2000, Toast.Type.WARNING);
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(getStage());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        TextField thresholdField = createNumberField();
        thresholdField.setPromptText("Wartość progowa (0–255)");

        Button applyButton = new Button("Zastosuj próg");
        Button cancelButton = new Button("Anuluj");

        applyButton.setOnAction(e -> {
            String thresholdText = thresholdField.getText().trim();
            if (thresholdText.isEmpty()) {
                Toast.makeText(getStage(), "Pole nie może być puste", 2000, Toast.Type.WARNING);
                return;
            }

            int threshold = Integer.parseInt(thresholdText);
            if (threshold < 0 || threshold > 255) {
                Toast.makeText(getStage(), "Wartość musi być z zakresu 0–255", 2000, Toast.Type.ERROR);
                return;
            }

            processedImage = applyThreshold(processedImage, threshold);
            processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
            isImageModified = true;
            Toast.makeText(getStage(), "Zastosowano progowanie", 2000, Toast.Type.SUCCESS);
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        layout.getChildren().addAll(new Label("Wartość progowa:"), thresholdField, applyButton, cancelButton);

        dialog.setScene(new Scene(layout));
        dialog.setTitle("Progowanie obrazu");
        dialog.showAndWait();
    }


    private BufferedImage applyThreshold(BufferedImage image, int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = (r + g + b) / 3;
                int binary = gray >= threshold ? 0xffffff : 0x000000;
                result.setRGB(x, y, binary);
            }
        }
        return result;
    }

    private BufferedImage applyContour(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int rgb = image.getRGB(x, y);
                int nextX = image.getRGB(x + 1, y);
                int nextY = image.getRGB(x, y + 1);

                int r = Math.abs(((rgb >> 16) & 0xff) - ((nextX >> 16) & 0xff)) +
                        Math.abs(((rgb >> 16) & 0xff) - ((nextY >> 16) & 0xff));
                int g = Math.abs(((rgb >> 8) & 0xff) - ((nextX >> 8) & 0xff)) +
                        Math.abs(((rgb >> 8) & 0xff) - ((nextY >> 8) & 0xff));
                int b = Math.abs((rgb & 0xff) - (nextX & 0xff)) +
                        Math.abs((rgb & 0xff) - (nextY & 0xff));

                int edgeColor = (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
                result.setRGB(x, y, edgeColor);
            }
        }

        return result;
    }

    @FXML
    private void handleRotateLeft(ActionEvent event) {
        if (processedImage == null && originalImage == null) {
            Toast.makeText(getStage(), "Brak obrazu do obrócenia", 2000, Toast.Type.WARNING);
            return;
        }
        BufferedImage imageToRotate = processedImage != null ? processedImage : originalImage;
        processedImage = rotateImage(imageToRotate, -90);
        processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
        isImageModified = true;
        Toast.makeText(getStage(), "Obraz został obrócony w lewo", 2000, Toast.Type.SUCCESS);
    }

    @FXML
    private void handleRotateRight(ActionEvent event) {
        if (processedImage == null && originalImage == null) {
            Toast.makeText(getStage(), "Brak obrazu do obrócenia", 2000, Toast.Type.WARNING);
            return;
        }
        BufferedImage imageToRotate = processedImage != null ? processedImage : originalImage;
        processedImage = rotateImage(imageToRotate, 90);
        processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
        isImageModified = true;
        Toast.makeText(getStage(), "Obraz został obrócony w prawo", 2000, Toast.Type.SUCCESS);
    }


    private BufferedImage rotateImage(BufferedImage img, int angleDegrees) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage rotatedImage = new BufferedImage(height, width, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                if (angleDegrees == 90) {
                    rotatedImage.setRGB(height - 1 - y, x, rgb);
                } else if (angleDegrees == -90) {
                    rotatedImage.setRGB(y, width - 1 - x, rgb);
                }
            }
        }

        return rotatedImage;
    }


    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }


    private BufferedImage applyGrayscale(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        return result;
    }

    private BufferedImage applyNegative(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = 255 - ((rgb >> 16) & 0xff);
                int g = 255 - ((rgb >> 8) & 0xff);
                int b = 255 - (rgb & 0xff);
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    private BufferedImage applyBlur(BufferedImage image) {
        // Dummy blur implementation: clone only
        return deepCopy(image);
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        return new BufferedImage(bi.getColorModel(), bi.copyData(null), bi.isAlphaPremultiplied(), null);
    }

    private Stage getStage() {
        return (Stage) originalImageView.getScene().getWindow();
    }
}
