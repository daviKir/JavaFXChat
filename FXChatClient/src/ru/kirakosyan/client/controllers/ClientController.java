package ru.kirakosyan.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.kirakosyan.client.ClientChat;
import ru.kirakosyan.client.models.Network;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class ClientController {

    @FXML private TextArea textArea;
    @FXML private TextField textField;
    @FXML private Button sendButton;
    @FXML public ListView<String> userList;

    public static final String PRIVATE_COMMAND = "/w";

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
            String newMessage = sender != null ? String.format("%s %s %s", PRIVATE_COMMAND, sender, message) : message;
            Network.getInstance().sendMessage(newMessage);
        } catch (IOException e) {
            application.showErrorDialog("Ошибка передачи данных по сети");
        }

        if (sender != null) {
            appendMessageToChat("Я", String.format(" -> %s%n%s", sender, message));
        } else {
            appendMessageToChat("Я", message);
        }
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
        Network.getInstance().waitMessages(message -> Platform.runLater(() -> {
            if (message.startsWith(PRIVATE_COMMAND)) {
                String[] parts = message.split(" ");
                String[] messageArr = new String[parts.length - 2];
                System.arraycopy(parts, 2, messageArr, 0, parts.length - 2);

                appendMessageToChat("server", String.join(" ", messageArr));
            } else {
                appendMessageToChat("server", message);
            }
        }));
    }
}
