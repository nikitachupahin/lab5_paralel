package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class HttpServer {
    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> {

                });
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
