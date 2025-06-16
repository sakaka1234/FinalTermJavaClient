package org.example.finaltermjava_client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.example.finaltermjava_client.model.Client;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class ClientChatController implements Initializable {
    @FXML
    private TextField tf_message;
    @FXML
    private ScrollPane sp_main;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ImageView choose_img;
    @FXML
    private ImageView btn_send;
    @FXML
    private Button stop_client;
    @FXML
    private ImageView audio_send;
    private Client client;
    static boolean recording = true;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // kết nối đến server
            client = new Client(new Socket("localhost", 2002), vbox_messages);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error Client", "Could not connect to server"));
        }
        btn_send.setOnMouseClicked(e -> sendMessage());
        tf_message.setOnAction(event -> sendMessage());
        choose_img.setOnMouseClicked(e -> chooseAndSendImage());
        audio_send.setOnMouseClicked(e -> handleAudioSend());
    }

    private void sendMessage() {
        String message = tf_message.getText();
        if (!message.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(message);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-background-color: rgb(15,125,242);-fx-background-radius: 20px;");
            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.945, 0.996));

            hBox.getChildren().add(textFlow);
            vbox_messages.getChildren().add(hBox);

            client.sendMessageToServer(message);
            tf_message.clear();
        }
    }
    public static void addLabel(String messageFromClient , VBox vBox){
        Platform.runLater(() -> {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5,5,5,10));

            Text text = new Text(messageFromClient);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
                    "-fx-background-radius: 20px;");
            textFlow.setPadding(new Insets(5,10,5,10));
            hBox.getChildren().add(textFlow);
            vBox.getChildren().add(hBox);
        });
    }
    private  void handleAudioSend(){
        new Thread(() ->{

            try{

                recording = true;
                AudioFormat format =new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100.0f,
                        16,
                        2,
                        4,
                        44100.0f,
                        false
                );
                // Check if the system supports the target data line
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Audio Error", "Audio recording not supported on this system."));
                    return;
                }

                TargetDataLine microphone =(TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();
                JOptionPane.showMessageDialog(null, "Click OK to start recording audio");

                byte[] buffer = new byte[4096];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                Thread recordingThread = new Thread(() -> {
                    while (recording) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                });
                recordingThread.start();
                JOptionPane.showMessageDialog(null, "Click OK to stop recording audio");

                recording = false;
                microphone.stop();
                microphone.close();
                recordingThread.join();
                //Encode to Base64
                String audioBase64 = Base64.getEncoder().encodeToString(out.toByteArray());
                String messageToSend = "[AUDIO]" + audioBase64;

                Platform.runLater(() -> {
                    addAudioToVBox(audioBase64, vbox_messages, Pos.CENTER_RIGHT);
                    client.sendMessageToServer(messageToSend);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Audio Error", "Could not record audio."));
            }
        }).start();
    }
    private void chooseAndSendImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(choose_img.getScene().getWindow());

        if(selectedFile != null){
            try {
                byte[] imageBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

                addImageToVBox(base64Image, vbox_messages, Pos.CENTER_RIGHT);

                client.sendMessageToServer("[IMAGE]" + base64Image);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not read image file");
            }
        }
    }
    public static void addImageToVBox(String base64Image, VBox vBox, Pos alignment) {
        Platform.runLater(() -> {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            Image image = new Image(new ByteArrayInputStream(imageBytes));

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200);
            imageView.setPreserveRatio(true);

            HBox hBox = new HBox(imageView);
            hBox.setAlignment(alignment);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            vBox.getChildren().add(hBox);
        });
    }
    public static void addAudioToVBox(String base64Audio, VBox vBox, Pos alignment) {
        Platform.runLater(() -> {
            try{
                HBox hBox = new HBox();
                hBox.setAlignment(alignment);
                hBox.setPadding(new Insets(5, 5, 5, 10));

                Button audioButton = new Button("🔊 Play Audio");
                audioButton.setStyle("-fx-background-color: lightgreen; -fx-background-radius: 20px;");
                audioButton.setOnAction(event -> playAudio(base64Audio));
                hBox.getChildren().add(audioButton);
                vBox.getChildren().add(hBox);
            }catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not play audio");
            }
        });
    }
    private static void playAudio(String base64Audio) {
        new Thread(() ->{
            try {
                byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(audioBytes);
                AudioFormat format =new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100.0f,
                        16,
                        2,
                        4,
                        44100.0f,
                        false
                );
                AudioInputStream audioInputStream = new AudioInputStream(byteStream, format, audioBytes.length);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Audio Error", "Could not play audio.");
            }
        }).start();


    }
    private static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
