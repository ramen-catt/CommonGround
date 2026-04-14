// HTTP server classes for serving the frontend and  endpoints
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
// Desktop/support for opening the map
import java.awt.Desktop;
// I/O
import java.io.IOException;
import java.io.OutputStream;
// Networking utilities for starting the local server
import java.net.InetSocketAddress;
import java.net.URI;
// String and file helpers
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
// JDBC classes for SQL stuff/database
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Collections used to store users and Common Ground locations
import java.util.ArrayList;
import java.util.List;

public class Main {

    // Holds one user from the account table after geocoding their address on the frontend
    record UserLocation(int id, String name, String address) {}

    // Holds one Common Ground / meetup location from the location table
    record CommonGround(int id, String name, String address, double lat, double lon, boolean vetted) {}

    // Update this database name if needed
    private static final String DB_URL = "jdbc:mysql://localhost:8889/CommonGround_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve the frontend files
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                sendFile(exchange, "src/Map.html", "text/html");
            } else if ("/Leaflet.js".equals(path)) {
                sendFile(exchange, "src/Leaflet.js", "application/javascript");
            } else {
                sendText(exchange, "Not found", 404, "text/plain");
            }
        });

        // Return all data needed by the frontend map
        server.createContext("/api/data", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, "Method not allowed", 405, "text/plain");
                return;
            }

            // Pull the two users from the account table
            UserLocation user1 = loadUserById(1);
            UserLocation user2 = loadUserById(2);

            // Pull all vetted Common Ground locations from the location table
            List<CommonGround> allCommonGrounds = loadCommonGrounds();

            // The midpoint is computed on the frontend after geocoding user addresses,
            // but we still return the user addresses here so Leaflet can geocode them.
            String json = buildDataJson(user1, user2, allCommonGrounds, loadLatestTransactionMeetup());

            sendText(exchange, json, 200, "application/json");
        });

        // Save the selected meetup location into the transactions table
        server.createContext("/api/select", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, "Method not allowed", 405, "text/plain");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int transactionId = parseIntField(body, "transactionId");
            String meetupAddress = parseStringField(body, "meetupAddress");

            updateTransactionMeetup(transactionId, meetupAddress, "selected");
            sendText(exchange, "{\"status\":\"ok\"}", 200, "application/json");
        });

        // confirm the selected meetup location
        server.createContext("/api/confirm", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, "Method not allowed", 405, "text/plain");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int transactionId = parseIntField(body, "transactionId");
            String meetupAddress = parseStringField(body, "meetupAddress");

            updateTransactionMeetup(transactionId, meetupAddress, "confirmed");
            sendText(exchange, "{\"status\":\"confirmed\"}", 200, "application/json");
        });

        server.start();

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create("http://localhost:8080/"));
        }

        System.out.println("Server running at http://localhost:8080/");
    }

    // Loads a user from the account table
    private static UserLocation loadUserById(int id) {
        String sql = """
                SELECT account_id, username, address
                FROM account
                WHERE account_id = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new UserLocation(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user from account table", e);
        }

        throw new RuntimeException("User not found: " + id);
    }

    // Loads all vetted Common Grounds from the location table
    private static List<CommonGround> loadCommonGrounds() {
        String sql = """
                SELECT location_id, address, latitude, longitude, name, vetted
                FROM location
                WHERE vetted = 1
                """;

        List<CommonGround> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(new CommonGround(
                        rs.getInt("location_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getBoolean("vetted")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load locations from location table", e);
        }

        return result;
    }

    // Gets the most recent meetup address stored in transactions, if any
    private static String loadLatestTransactionMeetup() {
        String sql = """
                SELECT meetup_address
                FROM transactions
                ORDER BY transaction_id DESC
                LIMIT 1
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getString("meetup_address");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load latest meetup address", e);
        }

        return null;
    }

    // Updates the meetup address and status for a transaction
    private static void updateTransactionMeetup(int transactionId, String meetupAddress, String status) {
        String sql = """
                UPDATE transactions
                SET meetup_address = ?, status = ?
                WHERE transaction_id = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, meetupAddress);
            statement.setString(2, status);
            statement.setInt(3, transactionId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction meetup", e);
        }
    }

    // Build the JSON response consumed by the frontend map
    private static String buildDataJson(
            UserLocation user1,
            UserLocation user2,
            List<CommonGround> commonGrounds,
            String selectedMeetupAddress
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"user1\":").append(userJson(user1)).append(",");
        sb.append("\"user2\":").append(userJson(user2)).append(",");
        sb.append("\"allCommonGrounds\":").append(commonGroundListJson(commonGrounds)).append(",");
        sb.append("\"selectedMeetupAddress\":")
                .append(selectedMeetupAddress == null ? "null" : quote(selectedMeetupAddress));
        sb.append("}");
        return sb.toString();
    }

    private static String userJson(UserLocation user) {
        return "{"
                + "\"id\":" + user.id() + ","
                + "\"name\":" + quote(user.name()) + ","
                + "\"address\":" + quote(user.address())
                + "}";
    }

    private static String commonGroundListJson(List<CommonGround> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            CommonGround cg = list.get(i);
            sb.append("{")
                    .append("\"id\":").append(cg.id()).append(",")
                    .append("\"name\":").append(quote(cg.name())).append(",")
                    .append("\"address\":").append(quote(cg.address())).append(",")
                    .append("\"lat\":").append(cg.lat()).append(",")
                    .append("\"lon\":").append(cg.lon()).append(",")
                    .append("\"vetted\":").append(cg.vetted())
                    .append("}");
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // JSON stuff
    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // Parses a numeric field from a simple JSON request body
    private static int parseIntField(String json, String fieldName) {
        String needle = "\"" + fieldName + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            throw new IllegalArgumentException("Missing field: " + fieldName);
        }
        start += needle.length();

        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }

        return Integer.parseInt(json.substring(start, end));
    }

    // Parses a string field from a simple JSON request body
    private static String parseStringField(String json, String fieldName) {
        String needle = "\"" + fieldName + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            throw new IllegalArgumentException("Missing field: " + fieldName);
        }

        start += needle.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (json.startsWith("null", start)) {
            return null;
        }

        if (json.charAt(start) != '"') {
            throw new IllegalArgumentException("Expected string field: " + fieldName);
        }

        start++;
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Unterminated string field: " + fieldName);
        }

        return json.substring(start, end);
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