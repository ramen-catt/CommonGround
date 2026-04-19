import Map.TransactionInfoPulling;
import Map.User;
import Map.LocationMapping;
import Map.CommonGroundLocation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// NOTE: This standalone server is replaced by MapServlet.java for the Tomcat deployment.
// MapServlet handles /get-meetup and /calculate-closest as proper servlet endpoints.
public class Main {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();

                if ("/".equals(path)) {
                    sendFile(exchange, "src/frontend/map.html", "text/html");
                } else if ("/Leaflet.js".equals(path)) {
                    sendFile(exchange, "src/frontend/Leaflet.js", "application/javascript");
                } else {
                    sendText(exchange, "Not found", 404, "text/plain");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        server.createContext("/get-meetup", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("id=")) {
                    int transactionId = Integer.parseInt(query.split("=")[1]);

                    TransactionInfoPulling puller = new TransactionInfoPulling();
                    User[] users = puller.pullUsers(transactionId);

                    if (users[0] != null && users[1] != null) {
                        String response = users[0].address + "|" + users[1].address;
                        sendText(exchange, response, 200, "text/plain");
                    } else {
                        sendText(exchange, "Transaction not found", 404, "text/plain");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try { sendText(exchange, "Server Error", 500, "text/plain"); } catch (IOException ignored) {}
            }
        });

        server.createContext("/calculate-closest", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String[] params = query.split("&");
                int id = Integer.parseInt(params[0].split("=")[1]);
                double latB = Double.parseDouble(params[1].split("=")[1]);
                double lonB = Double.parseDouble(params[2].split("=")[1]);
                double latS = Double.parseDouble(params[3].split("=")[1]);
                double lonS = Double.parseDouble(params[4].split("=")[1]);

                LocationMapping mapping = new LocationMapping();
                CommonGroundLocation closest = mapping.findClosestCG(latB, lonB, latS, lonS);
                mapping.updateTransactionMeetupLocation(id, closest.address);
                sendText(exchange, closest.name, 200, "text/plain");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        server.start();

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create("http://localhost:8081/"));
        }

        System.out.println("Server running at http://localhost:8081/");
    }

    private static void sendFile(HttpExchange exchange, String filePath, String contentType) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(filePath));
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendText(HttpExchange exchange, String text, int statusCode, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
