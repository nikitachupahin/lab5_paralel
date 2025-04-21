package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class HttpServer {
    private static final int PORT = 8080;
    private static final String DIRECTORY_PATH = "src/main/resources/";

    public static void main(String[] args) {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

            serverSocket.bind(new InetSocketAddress(PORT));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Non-blocking server started on port " + PORT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        handleAccept(serverSocket, selector);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleAccept(ServerSocketChannel serverSocket, Selector selector) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
    }

    private static void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        try {
            int bytesRead = client.read(buffer);
            if (bytesRead == -1) {
                client.close();
                return;
            }

            String request = new String(buffer.array()).trim();
            if (request.startsWith("GET")) {
                String path = parsePath(request);
                String fileName = path.equals("/page2.html") ? "page2.html" : "index.html";
                String content = readFile(fileName);

                String response;
                if (content.isEmpty()) {
                    String notFound = "<html><body><h1>404 Not Found</h1></body></html>";
                    response = buildHttpResponse("404 Not Found", notFound);
                } else {
                    response = buildHttpResponse("200 OK", content);
                }

                client.write(ByteBuffer.wrap(response.getBytes()));
                client.close();
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            try {
                client.close();
            } catch (IOException ignored) {}
        }
    }

    private static String parsePath(String request) {
        int start = request.indexOf("GET ") + 4;
        int end = request.indexOf(" HTTP/1.1");
        if (start != -1 && end != -1 && start < end) {
            return request.substring(start, end).trim();
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