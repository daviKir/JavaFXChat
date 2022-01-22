package ru.kirakosyan.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.kirakosyan.client.controllers.AuthController;
import ru.kirakosyan.client.controllers.ClientController;
import ru.kirakosyan.client.models.Network;

import java.io.IOException;

public class ClientChat extends Application {

    private static final String CONNECTION_ERROR_MESSAGE = "Невозможно установить сетевое соеденение";

    private Stage primatyStage;
    private Stage authStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primatyStage = stage;

        ClientController controller = createChatDialog(stage);
        createAuthDialog();

        controller.initializeMessageHandler();
    }

    private void createAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(getClass().getResource("views/authDialog.fxml"));
        AnchorPane authDialogPanel = authLoader.load();

        authStage = new Stage();
        authStage.initOwner(primatyStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
        AuthController authController = authLoader.getController();
        authController.setClientChat(this);
        authController.initializeMessageHandler();
        authStage.showAndWait();
    }

    private ClientController createChatDialog(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("views/chat-template.fxml"));

        Parent load = fxmlLoader.load();
        Scene scene = new Scene(load);

        this.primatyStage.setTitle("JavaFX chat");
        this.primatyStage.setScene(scene);

        ClientController controller = fxmlLoader.getController();

        controller.userList.getItems().addAll("username1", "username2", "username3");

        stage.show();

        connectToServer(controller);
        return controller;
    }

    private void connectToServer(ClientController clientController) {
        boolean result = Network.getInstance().connect();
        if (!result) {
            String errorMessage = CONNECTION_ERROR_MESSAGE;
            System.err.println(errorMessage);
            showErrorDialog(errorMessage);
            return;
        }

        clientController.setApplication(this);

        this.primatyStage.setOnCloseRequest((WindowEvent windowEvent) -> Network.getInstance().close());
    }

    public void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getChatStage() {
        return this.primatyStage;
    }
}
