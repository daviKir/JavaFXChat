package ru.kirakosyan.server.chat;

import ru.kirakosyan.clientserver.Command;
import ru.kirakosyan.server.chat.auth.AuthService;
import ru.kirakosyan.server.chat.auth.IAuthService;
import ru.kirakosyan.server.chat.auth.PersistentDbAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private IAuthService authService;

    public void start(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
            authService = createAuthService(false);
            authService.start();
            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    private IAuthService createAuthService(boolean local) {
        if (local) {
            return new AuthService();
        }

        return new PersistentDbAuthService();
    }

    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connections");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendCommand(Command.clientMessageCommand(sender.getUserName(), message));
            }
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUserName().equals(recipient)) {
                client.sendCommand(Command.privateMessageCommand(sender.getUserName(), privateMessage));
            }
        }
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(username)) {
                return true;
            }
        }

        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        this.clients.add(clientHandler);
        notifyClientUserListUpdated();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        this.clients.remove(clientHandler);
        notifyClientUserListUpdated();
    }

    public void notifyClientUserListUpdated() throws IOException {
        List<String> userListOnline = new ArrayList<>();
        for (ClientHandler client : clients) {
            userListOnline.add(client.getUserName());
        }

        for (ClientHandler client : clients) {
            client.sendCommand(Command.updateUserListCommand(userListOnline));
        }
    }

    public IAuthService getAuthService() {
        return authService;
    }
}
