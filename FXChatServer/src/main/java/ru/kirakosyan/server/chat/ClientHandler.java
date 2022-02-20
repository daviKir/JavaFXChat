package ru.kirakosyan.server.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kirakosyan.clientserver.Command;
import ru.kirakosyan.clientserver.CommandType;
import ru.kirakosyan.clientserver.commands.AuthCommandData;
import ru.kirakosyan.clientserver.commands.PrivateMessageCommandData;
import ru.kirakosyan.clientserver.commands.PublicMessageCommandData;
import ru.kirakosyan.clientserver.commands.UpdateUsernameCommandData;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private final static Logger LOGGER = LogManager.getLogger(MyServer.class);

    private static final int TIME_OUT = 120;

    private final MyServer server;
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String userName;
    private int curTime = TIME_OUT;

    public ClientHandler(MyServer myServer, Socket clientSocket) {
        this.server = myServer;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        server.getExecutorService().execute(()-> {
            try {
                Timer timer = waitConnection();
                authenticate(timer);
                readMessages();
            } catch (IOException e) {
                LOGGER.error("Failed to process message from client ");
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection");
                    e.printStackTrace();
                }
            }
        });
    }

    private Timer waitConnection() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                curTime--;
                if (curTime <= 0) {
                    try {
                        sendCommand(Command.endCommand(408, "Истекло время ожидания"));
                    } catch (IOException e) {
                        LOGGER.error("Failed to process message from client");
                        e.printStackTrace();
                    }
                    timer.cancel();
                }
            }
        }, 0, 1000);
        return timer;
    }

    private void authenticate(Timer timer) throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.AUTH){
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();

                String userName = server.getAuthService().getUserNameByLoginAndPassword(login, password);

                if (userName == null) {
                    sendCommand(Command.errorCommand("Не корректный логин и пароль"));
                } else if (server.isUsernameBusy(userName)) {
                    sendCommand(Command.errorCommand("Такой пользователь уже существует"));
                } else {
                    timer.cancel();
                    this.userName = userName;
                    sendCommand(Command.authOkCommand(userName));
                    server.subscribe(this);
                    return;
                }
                curTime = TIME_OUT;
            }
        }
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed ro read command class");
            e.printStackTrace();
        }

        return command;
    }

    private void readMessages() throws IOException {
        while (true){
            Command command = readCommand();

            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END: {
                    return;
                }
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String privateMessage = data.getMessage();
                    server.sendPrivateMessage(this, recipient, privateMessage);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    processMessage(data.getMessage());
                    break;
                }
                case UPDATE_USERNAME: {
                    UpdateUsernameCommandData data = (UpdateUsernameCommandData) command.getData();
                    String newUsername = data.getUsername();
                    server.getAuthService().updateUsername(userName, newUsername);
                    userName = newUsername;
                    server.notifyClientUserListUpdated();
                    break;
                }
            }
        }
    }

    private void processMessage(String message) throws IOException {
        this.server.broadcastMessage(message, this);
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }

    public String getUserName() {
        return userName;
    }
}
