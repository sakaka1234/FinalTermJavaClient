package org.example.finaltermjava_client.controller;



import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.finaltermjava_client.model.Database;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @FXML
    private TextField username;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private TextField password_visible;
    @FXML
    private Button btn_register;
    @FXML
    private Button change_to_login;
    @FXML
    private ImageView eye;
    private boolean isPasswordVisible = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ẩn textfield lúc ban đầu
        password_visible.setManaged(false);
        password_visible.setVisible(false);
        // Đồng bộ hai trường password
        password_visible.textProperty().bindBidirectional(password.textProperty());
        eye.setOnMouseClicked(this::togglePassWordVisibility);
        change_to_login.setOnAction(this::handleChangeToLogin);
        btn_register.setOnAction(this::handleRegister);
    }

    @FXML
    private void togglePassWordVisibility(MouseEvent event){
        isPasswordVisible =!isPasswordVisible;
        password_visible.setManaged(isPasswordVisible);
        password_visible.setVisible(isPasswordVisible);
        password.setManaged(!isPasswordVisible);
        password.setVisible(!isPasswordVisible);
    }
    @FXML
    private void handleChangeToLogin(ActionEvent event){
        try{
            FXMLLoader loader =new FXMLLoader(getClass().getResource("/org/example/finaltermjava_client/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) change_to_login.getScene().getWindow();
            stage.setScene(scene);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRegister(ActionEvent event){
        String user =username.getText();
        String mail = email.getText();
        String pass = password.getText();
        if(user.isEmpty() || pass.isEmpty() || mail.isEmpty()){
            showAlert(Alert.AlertType.WARNING,"Validation Error","Please fill in all fields");
            return;
        }
        if(!isValidEmail(mail)){
            showAlert(Alert.AlertType.WARNING, "Invalid Email","Please enter a valid email address");
            return;
        }
        if(pass.length() < 6){
            showAlert(Alert.AlertType.WARNING,"Weak Password","Password must be at least 6 characters long");
            return;
        }
        //Hash password
        String hasedPassword = BCrypt.withDefaults().hashToString(12,pass.toCharArray());
        //Save to database;
        try(Connection conn = Database.getConnection()){
            //check email is existed before;
            String checkSql = "SELECT COUNT(*) FROM user WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1,mail);
            ResultSet rs =checkStmt.executeQuery();
            if(rs.next() && rs.getInt(1) > 0){
                showAlert(Alert.AlertType.ERROR,"Email Exists","Email has already existed, Please choose another one");
                return;
            }
            String checkNameSql = "SELECT COUNT(*) FROM user WHERE username = ?";
            PreparedStatement checkName = conn.prepareStatement(checkNameSql);
            checkName.setString(1,user);
            ResultSet rsName = checkName.executeQuery();
            if(rsName.next() && rs.getInt(1) > 0){
                showAlert(Alert.AlertType.ERROR,"Username Exists","Username has already existed, Please choose another one");
                return;
            }
            String sql = "INSERT INTO user(username,email,password) VALUES (?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,user);
            stmt.setString(2,mail);
            stmt.setString(3,hasedPassword);
            stmt.executeUpdate();

            String role = "user";
            String imgPath = "/org/example/finaltermjava_client/Images/avatar.png";

            String fetchsql = "INSERT INTO fetchall(username,email,img,role) VALUES (?,?,?,?)";
            PreparedStatement fetchstmt = conn.prepareStatement(fetchsql);
            fetchstmt.setString(1,user);
            fetchstmt.setString(2,mail);
            fetchstmt.setString(3,imgPath);
            fetchstmt.setString(4,role);
            fetchstmt.executeUpdate();
            //chuyển sang admin form
            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION,"Success","User register successfully"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/finaltermjava_client/clientForm.fxml"));
            Scene scene = new Scene(loader.load());
            ClientFormController controller = loader.getController();
            controller.setInfoUser(user,imgPath);

            Stage stage = (Stage) btn_register.getScene().getWindow();
            stage.setScene(scene);
            clearForm();
        }catch (SQLException e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not register user.");
        } catch (IOException e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Change Scene Error", "Can not access to user page");
        }

    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        username.clear();
        password.clear();
    }

    //check input email whether suitable or not ?
    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(regex);
    }
}

