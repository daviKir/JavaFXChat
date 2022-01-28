package ru.kirakosyan.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.kirakosyan.client.controllers.AuthController;
import ru.kirakosyan.client.controllers.ClientController;

import java.io.IOException;

public class ClientChat extends Application {

    public static ClientChat INSTANCE;

    private FXMLLoader chatWindowLoader;
    private FXMLLoader authLoader;;
    private Stage primatyStage;
    private Stage authStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primatyStage = stage;

        initViews();
        getChatStage().show();
        getAuthStage().show();

        getAuthController().initializeMessageHandler();
    }

    @Override
    public void init() throws Exception {
        INSTANCE = this;
    }

    public void initViews() throws IOException {
        initChatWindow();
        initAuthDialog();
    }

    private void initAuthDialog() throws IOException {
        authLoader = new FXMLLoader();
        authLoader.setLocation(getClass().getResource("views/authDialog.fxml"));

        Parent authDialogPanel = authLoader.load();
        authStage = new Stage();
        authStage.initOwner(primatyStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
    }

    private void initChatWindow() throws IOException {
        chatWindowLoader = new FXMLLoader();
        chatWindowLoader.setLocation(getClass().getResource("views/chat-template.fxml"));

        Parent root = chatWindowLoader.load();
        this.primatyStage.setScene(new Scene(root));
    }

    private AuthController getAuthController() {
        return authLoader.getController();
    }

    private ClientController getChatController() {
        return chatWindowLoader.getController();
    }

    public void switchToMainChatWindow(String userName ) {
        getChatStage().setTitle(userName);
        getChatController().initializeMessageHandler();
        getAuthController().close();
        getAuthStage().close();
    }

    public void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getChatStage() {
        return this.primatyStage;
    }
}
