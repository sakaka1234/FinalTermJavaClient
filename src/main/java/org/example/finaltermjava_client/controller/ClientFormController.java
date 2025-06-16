package org.example.finaltermjava_client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientFormController implements Initializable {
    @FXML
    private Button open_chat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        open_chat.setOnAction(this::changeSceneToChat);
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
}
