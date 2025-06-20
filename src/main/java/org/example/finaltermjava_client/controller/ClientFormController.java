package org.example.finaltermjava_client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.finaltermjava_client.model.Database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ClientFormController implements Initializable {
    @FXML
    private Button open_chat;
    @FXML
    private ImageView client_img;
    @FXML
    private Label client_name;
    @FXML private DatePicker date_booking;
    @FXML private Button booking_btn;
    @FXML private ComboBox origin;
    @FXML private ComboBox destination;
    @FXML private ComboBox seat;

    @FXML private ImageView camera;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        open_chat.setOnAction(this::changeSceneToChat);
        booking_btn.setOnAction(this::booking);
        origin.getItems().addAll("Da Nang", "Hai Phong", "Ha Noi", "Sai Gon", "Hue");
        destination.getItems().addAll("Da Nang", "Hai Phong", "Ha Noi", "Sai Gon", "Hue");
        seat.getItems().addAll("A11","A12","A13","A14","A15","A16","A17","A18","A19");

        camera.setOnMouseClicked(event -> chooseImage());
    }

    @FXML
    private void changeSceneToChat(ActionEvent event){
        try{
            FXMLLoader loader =new FXMLLoader(getClass().getResource("/org/example/finaltermjava_client/clientChat.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) open_chat.getScene().getWindow();
            stage.setScene(scene);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void setInfoUser(String name , String imgPath){
        client_name.setText(name);
        try{
            Image image;
            if(imgPath.startsWith("/")){
                image = new Image(getClass().getResourceAsStream(imgPath));
            }else {
                File file = new File(imgPath);
                if(file.exists()){
                    image =new Image(file.toURI().toString());
                }else{
                    throw new IOException("Image file not found");
                }
            }
            client_img.setImage(image);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void booking(ActionEvent event){
        String user = client_name.getText();
        LocalDate selectedDate = date_booking.getValue();
        String getOrigin = (String) origin.getValue();
        String getDestination = (String) destination.getValue();
        String getSeat = (String) seat.getValue();
        if(selectedDate == null || getOrigin.isEmpty() || getDestination.isEmpty()){
            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING,"Missing fields","Please fill all fields"));
            return;
        }
        try(Connection conn = Database.getConnection()){
            String sql = "INSERT INTO trainticket(username,date,origin,destination,seat) VALUES (?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,user);
            stmt.setDate(2, Date.valueOf(selectedDate));
            stmt.setString(3,getOrigin);
            stmt.setString(4,getDestination);
            stmt.setString(5,getSeat);
            stmt.executeUpdate();
            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION,"Success","Booking successfully"));
        }catch (SQLException e){
            showAlert(Alert.AlertType.ERROR,"Database error","Some thing go wrong here :<");
            e.printStackTrace();

        }

    }
    private void chooseImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image files","*.png","*.jpeg","*.jpg","*.gif"));
        File selectedFile = fileChooser.showOpenDialog(client_img.getScene().getWindow());
        if(selectedFile != null){
            try(Connection conn = Database.getConnection()){
                String imagePath = selectedFile.getAbsolutePath();
                Image image = new Image(selectedFile.toURI().toString());
                client_img.setImage(image);
                makeImageViewCircular(client_img);

                String sql = "UPDATE fetchall SET img = ? WHERE username = ? and role = 'user'";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1,imagePath);
                stmt.setString(2,client_name.getText());
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Avatar updated successfully.");
            }catch (SQLException e){
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update avatar path.");
            }catch (Exception e){
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Image Error", "Failed to load image.");
            }
        }
    }

    private void makeImageViewCircular(ImageView imageView){
        double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
        Circle clip = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, radius);
        imageView.setClip(clip);
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
