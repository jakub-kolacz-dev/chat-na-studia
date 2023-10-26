package com.example.demo12;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClientJavaFX extends Application {

    private static final String HOST = "localhost";
    private static final int PORT = 55554;

    private Socket clientSocket;
    private String nickname;

    private TextArea chatArea;
    private TextField messageField;
    private String[] onlineUsers;

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


        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Menu");

        MenuItem activeUsersItem = new MenuItem("Użytkownicy Online");
        activeUsersItem.setOnAction(e -> displayUserList(onlineUsers));

        menu.getItems().add(activeUsersItem);
        menuBar.getMenus().add(menu);
        root.setTop(menuBar);

        primaryStage.setOnCloseRequest(e -> {
            disconnect();
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        if (promptForNickname()) {
            nickname = promptForNicknameInput();
        }

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
                if (message.startsWith("UPDATE_USERS|")) {
                    handleUserListUpdate(message.replace("UPDATE_USERS|", ""));
                } else {
                    displayMessage(message);
                }
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

    private void handleUserListUpdate(String message) {
        String[] parts = message.split("\\|");
        if (parts.length >= 1) {
            onlineUsers = parts;
        }
    }

    private void displayUserList(String[] users) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Użytkownicy Online");
            alert.setHeaderText("Lista aktywnych użytkowników:");

            TextArea userArea = new TextArea(String.join("\n", users));
            userArea.setEditable(false);
            userArea.setWrapText(true);
            alert.getDialogPane().setContent(userArea);

            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}


