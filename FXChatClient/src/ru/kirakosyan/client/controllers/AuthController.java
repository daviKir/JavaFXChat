package ru.kirakosyan.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.kirakosyan.client.ClientChat;
import ru.kirakosyan.client.models.Network;

import java.io.IOException;

public class AuthController {

    public static final String AUTH_COMMAND = "/auth";
    public static final String AUTH_OK_COMMAND = "/authOk";
    public static final String SEPARATOR = " ";

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button authButton;

    private ClientChat clientChat;

    public void executeAuth() {
        String login = loginField.getText();
        String password = passwordField.getText();
        boolean isError = login == null || login.isEmpty() || password == null || password.isEmpty();

        if (isError) {
            clientChat.showErrorDialog("Логин и пароль должны быть указаны");
            return;
        }

        String authCommandMessage = String.format("%s %s %s", AUTH_COMMAND, login, password);

        try {
            Network.getInstance().sendMessage(authCommandMessage);
        } catch (IOException e) {
            clientChat.showErrorDialog("Ошибка передачи данных по сети");
            e.printStackTrace();
        }
    }

    public void setClientChat(ClientChat clientChat) {
        this.clientChat = clientChat;
    }

    public void initializeMessageHandler() {
        Network.getInstance().waitMessages((String message) -> {
            if (message.startsWith(AUTH_OK_COMMAND)) {
                String[] parts = message.split(SEPARATOR);
                String userName = parts[1];
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    clientChat.getChatStage().setTitle(userName);
                    clientChat.getAuthStage().close();
                });
            } else {
                Platform.runLater(() -> clientChat.showErrorDialog("Пользователя с таким логином и паролем не существует"));
            }
        });
    }
}
