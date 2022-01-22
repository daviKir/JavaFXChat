package ru.kirakosyan.server.chat;

import ru.kirakosyan.server.chat.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    public static final String SEPARATOR = " ";

    private final List<ClientHandler> clients = new ArrayList<>();
    private AuthService authService;

    public void start(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
            authService = new AuthService();
            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connections");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public void broadcastMessage(String message, ClientHandler sender, boolean isPrivet) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                if (isPrivet) {
                    String[] parts = message.split(SEPARATOR);
                    String userName = parts[1];
                    if (client.getUserName().equals(userName)) {
                        client.sendMessage(message);
                    }
                } else {
                    client.sendMessage(message);
                }
            }
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        this.clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        this.clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
