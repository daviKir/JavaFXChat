package ru.kirakosyan.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.kirakosyan.client.ClientChat;
import ru.kirakosyan.client.dialogs.Dialog;
import ru.kirakosyan.client.models.Network;
import ru.kirakosyan.client.models.ReadCommandListener;
import ru.kirakosyan.clientserver.Command;
import ru.kirakosyan.clientserver.CommandType;
import ru.kirakosyan.clientserver.commands.AuthOkCommandData;
import ru.kirakosyan.clientserver.commands.EndCommandData;
import ru.kirakosyan.clientserver.commands.ErrorCommandData;

import java.io.IOException;

public class AuthController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button authButton;

    private ReadCommandListener readMessageListener;

    public void executeAuth() {
        String login = loginField.getText();
        String password = passwordField.getText();
        boolean isError = login == null || login.isEmpty() || password == null || password.isEmpty();

        if (isError) {
            Dialog.AuthError.EMPTY_CREDENTIALS.show();
            return;
        }

        if (!connectToServer()) {
            Dialog.NetworkError.SERVER_CONNECT.show();
        }

        try {
            Network.getInstance().sendAuthMessage(login, password);
        } catch (IOException e) {
            Dialog.NetworkError.SEND_MESSAGE.show();
            e.printStackTrace();
        }
    }

    private boolean connectToServer() {
        Network network = Network.getInstance();
        return network.isConnected() || network.connect();
    }

    public void initializeMessageHandler() {
        readMessageListener = getNetwork().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.AUTH_OK) {
                    AuthOkCommandData data = (AuthOkCommandData) command.getData();
                    String userName = data.getUsername();
                    Network.getInstance().setCurrentUsername(userName);
                    Platform.runLater(() -> ClientChat.INSTANCE.switchToMainChatWindow(userName));
                } else if (command.getType() == CommandType.ERROR) {
                    ErrorCommandData data = (ErrorCommandData) command.getData();
                    String errorMessage = data.getErrorMessage();
                    Platform.runLater(() -> Dialog.AuthError.INVALID_CREDENTIALS.show(errorMessage));
                } else if (command.getType() == CommandType.END) {
                    EndCommandData data = (EndCommandData) command.getData();
                    String message = data.getMessage();
                    int code = data.getCode();
                    Platform.runLater(() -> {
                        Dialog.AuthError.INVALID_CREDENTIALS.show(code + " " + message);
                        Platform.exit();
                    });
                }
            }
        });
    }

    public void close() {
        getNetwork().removeReadMessageListener(readMessageListener);
    }

    private Network getNetwork() {
        return Network.getInstance();
    }
}
