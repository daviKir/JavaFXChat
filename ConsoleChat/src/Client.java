import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static final int PORT = 2020;
    private static final String HOST = "localhost";
    private static final String APP_NAME = "client";

    private static final String  INVALID_HOST_MESSAGE = "Invalid host: ";
    private static final String  ERROR_CONNECTION_MESSAGE = "Connection error";
    private static final String  SUCCESS_CONNECTION_MESSAGE = "Connection success";

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println(SUCCESS_CONNECTION_MESSAGE);

            InputOutputController inputOutputController = new InputOutputController(socket, APP_NAME);
            inputOutputController.init();
            inputOutputController.startInputListener();
            inputOutputController.startOutputListener();

        } catch (ControllerInitializedError controllerInitializedError) {
            System.err.println(controllerInitializedError.getMessage());
        } catch (UnknownHostException e) {
            System.err.println(INVALID_HOST_MESSAGE + HOST);
        } catch (IOException e) {
            System.err.println(ERROR_CONNECTION_MESSAGE);
        }
    }

}
