package com.mycompany.app;

import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Hello world!
 */
public class App {

    private static final String MESSAGE = "Hello World!";

    public App() {}

    public static void main(String[] args) throws Exception {
        System.out.println(MESSAGE + " (starting UI server on http://localhost:8080/)");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Simple JSON/plain endpoint
        server.createContext("/api/hello", exchange -> {
            String response = MESSAGE;
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        // Static file handler: serves resources from /static on the classpath
        server.createContext("/", exchange -> {
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            InputStream is = App.class.getResourceAsStream("/static" + path);
            if (is == null) {
                String notFound = "404 Not Found";
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }
            String contentType = guessContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            byte[] data = is.readAllBytes();
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            } finally {
                is.close();
            }
        });

        server.setExecutor(null);
        server.start();
    }

    private static String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    public String getMessage() {
        return MESSAGE;
    }
}
