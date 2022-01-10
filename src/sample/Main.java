package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("sample.fxml"));
        Parent load = fxmlLoader.load();
        Scene scene = new Scene(load);

        stage.setTitle("JavaFX chat");
        stage.setScene(scene);

        Controller controller = fxmlLoader.getController();
        controller.userList.getItems().addAll("user1", "user2");

        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
