package com.example.demo12;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Chat2 extends Application {
    private static final String HOST = "localhost";
    private static final int PORT = 55554;

    private Socket clientSocket;
    private String nickname;

    private TextArea chatArea;
    private TextField messageField;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Czat");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        root.setCenter(chatArea);


        messageField = new TextField();
        messageField.setOnAction(e -> sendMessage());
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());
        HBox messageBox = new HBox(messageField, sendButton);
        messageBox.setSpacing(10);
        messageBox.setAlignment(Pos.CENTER);
        root.setBottom(messageBox);

        primaryStage.setOnCloseRequest(e -> {
            disconnect();
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        if(promptForNickname()){
            nickname = promptForNicknameInput();

        };
        connect();
    }

    private void connect() {
        try {
            clientSocket = new Socket(HOST, PORT);

            clientSocket.getOutputStream().write(nickname.getBytes());

            Thread receiveThread = new Thread(this::receiveMessages);
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                displayMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = nickname + ": " + messageField.getText();
        messageField.setText("");
        try {
            clientSocket.getOutputStream().write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    private boolean promptForNickname() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nickname");
        alert.setHeaderText("Czy chcesz podać swój nickname?");
        ButtonType yesButton = new ButtonType("Tak", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Nie", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        alert.initOwner(messageField.getScene().getWindow());
        return alert.showAndWait().orElse(noButton) == yesButton;
    }

    private String promptForNicknameInput() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nickname");
        dialog.setHeaderText("Podaj swój nickname:");
        dialog.initOwner(messageField.getScene().getWindow());
        return dialog.showAndWait().orElse("");
    }



    private void disconnect() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPopupWindow() {
        Stage popupStage = new Stage();

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Wprowadzanie nickname");

        Button actionButton = new Button("Podaj nickname");
        actionButton.setOnAction(e -> {
            promptForNickname();
            popupStage.close();
        });

        Button closeButton = new Button("Zamknij");
        closeButton.setOnAction(e -> popupStage.close());

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(actionButton, closeButton);

        Scene scene = new Scene(vbox, 200, 100);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
