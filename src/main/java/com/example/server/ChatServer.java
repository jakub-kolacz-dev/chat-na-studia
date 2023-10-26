package com.example.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ChatServer {


    private static final int PORT = 55554;

    private ServerSocket serverSocket;
    private Map<Socket, String> clients;
    private Set<String> nicknames;


    public ChatServer() {
        clients = new HashMap<>();
        nicknames = new HashSet<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowe połączenie: " + clientSocket.getRemoteSocketAddress());

                String nickname = receiveNickname(clientSocket);

                nicknames.add(nickname);
                clients.put(clientSocket, nickname);

                broadcastMessage(nickname + " dołączył do czatu!\n");

                clientSocket.getOutputStream().write("Połączony z serwerem!\n".getBytes());
                clientSocket.getOutputStream().write(getOnlineUsers().getBytes());

                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receiveNickname(Socket clientSocket) {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead = clientSocket.getInputStream().read(buffer);
            return new String(buffer, 0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                int bytesRead = clientSocket.getInputStream().read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                String message = new String(buffer, 0, bytesRead);
                broadcastMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String nickname = clients.get(clientSocket);
        broadcastMessage(nickname + " opuścił czat!");
        clients.remove(clientSocket);
        nicknames.remove(nickname);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        message+="\n";
        for (Socket clientSocket : clients.keySet()) {
            try {
                clientSocket.getOutputStream().write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getOnlineUsers() {
        return "Użytkownicy online: " + String.join(", ", nicknames)+"\n";
    }

    public void stop() {
        try {
            for (Socket clientSocket : clients.keySet()) {
                clientSocket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean isPortAvailable(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    // Ignorowanie błędów podczas zamykania gniazda
                }
            }
        }
        return false;
    }




    public static void main(String[] args) {
        if(isPortAvailable(PORT)) {
            ChatServer server = new ChatServer();
            server.start();
        }
    }
}

