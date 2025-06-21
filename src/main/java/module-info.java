module org.example.finaltermjava_client {
    requires javafx.fxml;
    requires java.sql;
    requires bcrypt;
    requires org.commonmark;
    requires com.google.gson;
    requires javafx.web;
    requires java.desktop;
    requires java.net.http;


    opens org.example.finaltermjava_client to javafx.fxml;
    opens org.example.finaltermjava_client.controller to javafx.fxml;
    exports org.example.finaltermjava_client;
    exports org.example.finaltermjava_client.utils;
}