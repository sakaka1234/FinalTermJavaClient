module org.example.finaltermjava_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires bcrypt;
    requires java.desktop;


    opens org.example.finaltermjava_client to javafx.fxml;
    opens org.example.finaltermjava_client.controller to javafx.fxml;
    exports org.example.finaltermjava_client;
}