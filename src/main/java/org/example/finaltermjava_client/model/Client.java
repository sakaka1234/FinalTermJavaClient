package org.example.finaltermjava_client.model;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import org.example.finaltermjava_client.controller.ClientChatController;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private VBox vboxMessages;

    public Client(Socket socket, VBox vboxMessages) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.vboxMessages = vboxMessages;
            listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void listenForMessages() {
        new Thread(() ->{
            String msgFromServer;
            try{
                while((msgFromServer = reader.readLine()) != null){
                    String finalMsgFromServer = msgFromServer;
                    Platform.runLater(() -> {
                        if(finalMsgFromServer.startsWith("[IMAGE]")){
                            String base64Image = finalMsgFromServer.substring(7).replaceAll("\\s+", "");
                            ClientChatController.addImageToVBox(base64Image, vboxMessages, Pos.CENTER_LEFT);
                        }
                        else if (finalMsgFromServer.startsWith("[AUDIO]")) {
                            String base64Audio = finalMsgFromServer.substring(7).replaceAll("\\s+", "");
                            ClientChatController.addAudioToVBox(base64Audio, vboxMessages, Pos.CENTER_LEFT);
                        }
                        else{
                            ClientChatController.addLabel(finalMsgFromServer, vboxMessages);
                        }
                    });
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
    }
}
