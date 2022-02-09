package ru.kirakosyan.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.kirakosyan.client.ClientChat;
import ru.kirakosyan.client.models.Network;
import ru.kirakosyan.clientserver.commands.ClientMessageCommandData;
import ru.kirakosyan.clientserver.commands.PrivateMessageCommandData;
import ru.kirakosyan.clientserver.commands.UpdateUserListCommandData;
import ru.kirakosyan.client.dialogs.Dialog;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

public class ClientController {

    @FXML private TextArea textArea;
    @FXML private TextField textField;
    @FXML private Button sendButton;
    @FXML public ListView<String> userList;

    private ClientChat application;

    public void sendMessage() {

        String message = textField.getText().trim();

        if (message.isEmpty()){
            textField.clear();
            return;
        }

        String sender = null;
        if (!userList.getSelectionModel().isEmpty()) {
            sender = userList.getSelectionModel().getSelectedItem();
        }

        try {
            if (sender != null) {
                Network.getInstance().sendPrivateMessage(sender, message);
            } else {
                Network.getInstance().sendMessage(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
            application.showErrorDialog("Ошибка передачи данных по сети");
        }

        appendMessageToChat("Я", message);
    }

    private void appendMessageToChat(String sender,  String message) {
        textArea.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        textArea.appendText(System.lineSeparator());

        if (sender != null) {
            textArea.appendText(sender + ":");
            textArea.appendText(System.lineSeparator());
        }

        textArea.appendText(message);
        textArea.appendText(System.lineSeparator());
        textArea.appendText(System.lineSeparator());
        textField.setFocusTraversable(true);
        textField.clear();
    }

    public void setApplication(ClientChat application) {
        this.application = application;
    }

    public void initializeMessageHandler() {
        Network.getInstance().addReadMessageListener(command -> {
            switch (command.getType()) {
                case CLIENT_MESSAGE: {
                    ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                    appendMessageToChat(data.getSender(), data.getMessage());
                    break;
                }
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    appendMessageToChat(data.getReceiver(), data.getMessage());
                    break;
                }
                case UPDATE_USER_LIST: {
                    UpdateUserListCommandData data = (UpdateUserListCommandData) command.getData();
                    Platform.runLater(() -> userList.setItems(FXCollections.observableList(data.getUsers())));
                }
            }
        });
    }

    public void closeChat(ActionEvent actionEvent) {
        ClientChat.INSTANCE.getChatStage().close();
    }

    public void changeUserName(ActionEvent actionEvent) {
        TextInputDialog editDialog = new TextInputDialog();
        editDialog.setTitle("Изменить юзернейм");
        editDialog.setHeaderText("Введите новый юзернейм");
        editDialog.setContentText("Username:");

        Optional<String> result = editDialog.showAndWait();
        if (result.isPresent()) {
            try {
                Network.getInstance().changeUsername(result.get());
            } catch (IOException e) {
                e.printStackTrace();
                Dialog.NetworkError.SEND_MESSAGE.show();
            }

        }
    }

    public void about(ActionEvent actionEvent) {
        Dialog.AboutDialog.INFO.show();
    }
}
