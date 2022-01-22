import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/*
* Т.к классы клиента и сервера реализованны в одном проекте, чтобы не дублировать код,
* вынес повторяющуюся часть логики в отдельный класс
* */
public class InputOutputController {

    private static final String  DISCONNECT_COMMAND = "/end";
    private static final String  ERROR_CONNECTION_MESSAGE = "Connection error";
    private static final String  ERROR_INITIALIZATION_MESSAGE = "Not initialized controller";

    private final Socket socket;
    private final String thisAppName;
    private String connectedAppName = "";
    private DataOutputStream output;
    private DataInputStream input;
    private boolean initialized = false;

    public InputOutputController(Socket socket, String appName) {
        this.thisAppName = appName;
        this.socket = socket;
    }

    public void init() {
        try {
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            output.writeUTF(thisAppName);
            initialized = true;
        } catch (IOException e) {
            System.err.println(ERROR_CONNECTION_MESSAGE);
        }
    }

    public void startInputListener() throws ControllerInitializedError {
        if (!initialized) {
            throw new ControllerInitializedError(ERROR_INITIALIZATION_MESSAGE);
        }

        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = input.readUTF();
                        if (connectedAppName.isEmpty()) {
                            setConnectedAppName(message);
                        } else {
                            if (message.equals(DISCONNECT_COMMAND)) {
                                break;
                            }
                            printMessage(message);
                        }
                    }
                } catch (IOException e) {
                    System.err.println(ERROR_CONNECTION_MESSAGE);
                }
            }

            private void printMessage(String message) {
                for (int i = 0; i < thisAppName.length() + 2; i++) {
                    System.out.print("\b");
                }
                System.out.printf("%s: %s%n%s> ", connectedAppName, message, thisAppName);
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    public void startOutputListener() throws ControllerInitializedError {
        if (!initialized) {
            throw new ControllerInitializedError(ERROR_INITIALIZATION_MESSAGE);
        }

        try {
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String prefix = thisAppName + "> ";
                System.out.print(prefix);
                String message = scanner.nextLine();
                if (message.equals(DISCONNECT_COMMAND)) {
                    break;
                } else if (!message.equals(prefix)) {
                    output.writeUTF(message);
                }
            }
        } catch (IOException e) {
            System.err.println(ERROR_CONNECTION_MESSAGE);
        }
    }

    private void setConnectedAppName(String name) {
        this.connectedAppName = name;
    }

}
