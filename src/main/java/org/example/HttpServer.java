package org.example;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class HttpServer {
    private static final int PORT = 8080;
    private static final String DIRECTORY_PATH = "src/main/resources/";
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleRequest(clientSocket));
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String line = in.readLine();
            if (line != null && line.startsWith("GET")) {
                String path = parsePath(line);
                String filePath = path.equals("/page2.html") ? "page2.html" : "index.html";
                String content = readFile(filePath);

                if (content.isEmpty()) {
                    String notFoundMessage = "<html><body><h1>404 Not Found</h1></body></html>";
                    String response = buildHttpResponse("404 Not Found", notFoundMessage);
                    out.write(response.getBytes());
                } else {
                    String response = buildHttpResponse("200 OK", content);
                    out.write(response.getBytes());
                }
            }

        } catch (IOException e) {
            System.err.println("Request handling failed: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private static String parsePath(String requestLine) {
        int start = requestLine.indexOf("GET ") + 4;
        int end = requestLine.indexOf(" HTTP/1.1");
        if (start != -1 && end != -1 && start < end) {
            return requestLine.substring(start, end).trim();
        }
        return "/";
    }

    private static String readFile(String fileName) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(DIRECTORY_PATH + fileName));
            return new String(encoded);
        } catch (IOException e) {
            return "";
        }
    }

    private static String buildHttpResponse(String status, String body) {
        return "HTTP/1.1 " + status + "\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                body;
    }
}