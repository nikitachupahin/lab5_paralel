package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HttpClient {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of clients: ");
        int numClients = scanner.nextInt();

        List<Thread> clients = new ArrayList<>();

        for (int i = 0; i < numClients; i++) {
            Thread clientThread = new Thread(() -> {
                sendRequest("/index.html");
                sendRequest("/page2.html");
            });
            clients.add(clientThread);
            clientThread.start();
        }

        for (Thread client : clients) {
            try {
                client.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }
    }

    private static void sendRequest(String path) {
        try (Socket socket = new Socket(SERVER_IP, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String request = "GET " + path + " HTTP/1.1\r\n" +
                    "Host: " + SERVER_IP + "\r\n" +
                    "Connection: close\r\n\r\n";
            out.print(request);
            out.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            System.out.println("Received response for " + path + ":\n" + response);

        } catch (IOException e) {
            System.err.println("Error sending request to " + path + ": " + e.getMessage());
        }
    }
}