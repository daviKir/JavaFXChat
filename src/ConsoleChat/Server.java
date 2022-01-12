package ConsoleChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 2020;
    private static final String APP_NAME = "server";

    private static final String  WAIT_CONNECT_MESSAGE = "Wait connect...";
    private static final String  SUCCESS_CONNECTION_MESSAGE = "Connection success";
    private static final String  ERROR_CONNECTION_MESSAGE = "Connection error";
    private static final String  DISCONNECT_MESSAGE = "Disconnection";

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println(WAIT_CONNECT_MESSAGE);
            Socket socket = serverSocket.accept();
            System.out.println(SUCCESS_CONNECTION_MESSAGE);

            InputOutputController inputOutputController = new InputOutputController(socket, APP_NAME);
            inputOutputController.init();
            inputOutputController.startInputListener();
            inputOutputController.startOutputListener();

        } catch (ControllerInitializedError controllerInitializedError) {
            System.err.println(controllerInitializedError.getMessage());
        } catch (IOException e) {
            System.err.println(ERROR_CONNECTION_MESSAGE);
            e.printStackTrace();
        } finally {
            System.out.println(DISCONNECT_MESSAGE);
        }
    }
}
