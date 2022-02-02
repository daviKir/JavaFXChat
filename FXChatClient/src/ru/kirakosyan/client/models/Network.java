package ru.kirakosyan.client.models;

import ru.kirakosyan.clientserver.Command;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Network {

    private List<ReadCommandListener> listeners = new CopyOnWriteArrayList<>();

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8189;

    private int port;
    private  String host;
    private Socket socket;
    private ObjectInputStream socketInput;
    private ObjectOutputStream socketOutput;

    private static Network INSTANCE;
    private Thread readMessageProcess;
    private boolean connected;

    public static Network getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Network();
        }
        return INSTANCE;
    }

    private Network(int port, String host) {
        this.port = port;
        this.host = host;
    }

    private Network() {
        this(SERVER_PORT, SERVER_HOST);
    }

    public boolean connect() {
        try {
            this.socket = new Socket(this.host, this.port);
            this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
            this.socketInput = new ObjectInputStream(socket.getInputStream());
            readMessageProcess = startReadMessageProcess();
            this.connected = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось создать соеденение");
            return false;
        }
    }

    public void sendAuthMessage(String login, String password) throws IOException {
        sendCommand(Command.authCommand(login, password));
    }

    public void sendMessage(String message) throws IOException {
        sendCommand(Command.publicMessageCommand(message));
    }

    private void sendCommand(Command command) throws IOException {
        try {
            socketOutput.writeObject(command);
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение на сервер");
            e.printStackTrace();
            throw e;
        }
    }

    public void sendPrivateMessage(String recipient, String message) throws IOException {
        sendCommand(Command.privateMessageCommand(recipient, message));
    }

    public Thread startReadMessageProcess() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    Command command = createCommand();

                    for (ReadCommandListener messageListener : listeners) {
                        messageListener.processReceivedCommand(command);
                    }
                } catch (IOException e) {
                    System.err.println("Не удалось прочитать сообщение от сервера");
                    e.printStackTrace();
                    close();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private Command createCommand() throws IOException {
        Command command = null;

        try {
            command = (Command) socketInput.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read command class");
            e.printStackTrace();
        }

        return command;
    }

    public ReadCommandListener addReadMessageListener(ReadCommandListener listener) {
        listeners.add(listener);
        return listener;

    }

    public void removeReadMessageListener(ReadCommandListener listener) {
        listeners.remove(listener);
    }

    public void changeUsername(String newUsername) throws IOException {
        sendCommand(Command.updateUsernameCommand(newUsername));
    }

    public void close() {
        try {
            connected = false;
            readMessageProcess.interrupt();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
