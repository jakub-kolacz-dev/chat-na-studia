package com.example.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Objects.*;

public class ConcurrentChatServer {

    private final int PORT = 55554;
    private ServerSocket serverSocket;
    private List<String> nicknames;
    private ConcurrentHashMap<Socket, String> clients;
    private ExecutorService pool;

    public ConcurrentChatServer() {
        nicknames = Collections.synchronizedList(new ArrayList<>());
        clients = new ConcurrentHashMap<>();
        pool = Executors.newFixedThreadPool(100);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowe połączenie: " + clientSocket.getRemoteSocketAddress());


                pool.execute(() -> {
                    try {
                        String nickname = receiveNickname(clientSocket);

                        nicknames.add(nickname);
                        clients.put(clientSocket, requireNonNull(nickname));

                        broadcastMessage(nickname + " dołączył do czatu!\n");
                        broadcastUpdatedUsers();

                        clientSocket.getOutputStream().write("Połączony z serwerem!\n".getBytes());
                        clientSocket.getOutputStream().write(getOnlineUsers().getBytes());

                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receiveNickname(Socket clientSocket) throws IOException {

        String user = "";
        try {
            byte[] buffer = new byte[1024];
            int bytesRead = clientSocket.getInputStream().read(buffer);
            user = new String(buffer, 0, bytesRead);
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void broadcastMessage(String message) {
        message += "\n";
        for (Socket clientSocket : clients.keySet()) {
            try {
                clientSocket.getOutputStream().write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getOnlineUsers() {
        return "Użytkownicy online: " + String.join(", ", nicknames) + "\n";
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
        } finally {

            String nickname = clients.get(clientSocket);
            clients.remove(clientSocket);
            nicknames.remove(nickname);
            broadcastUpdatedUsers();
            broadcastMessage(nickname + " opuścił czat!");
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastUpdatedUsers() {
        String userListMessage = "UPDATE_USERS|" + String.join(", ", nicknames);
        broadcastMessage(userListMessage);
    }

    public static void main(String[] args) {
        new ConcurrentChatServer().start();
    }
}
