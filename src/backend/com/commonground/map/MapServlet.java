package com.commonground.map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/api/map/*")
public class MapServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CommonGround_db";
    private static final String DB_USER = "cguser";
    private static final String DB_PASS = "cgpass123";

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        try {
            String path = req.getPathInfo();
            if (path == null || "/locations".equals(path)) {
                out.print(gson.toJson(getVettedLocations()));
            } else if ("/closest".equals(path)) {
                double buyerLat = Double.parseDouble(req.getParameter("buyerLat"));
                double buyerLng = Double.parseDouble(req.getParameter("buyerLng"));
                double sellerLat = Double.parseDouble(req.getParameter("sellerLat"));
                double sellerLng = Double.parseDouble(req.getParameter("sellerLng"));
                out.print(gson.toJson(findClosestLocation(buyerLat, buyerLng, sellerLat, sellerLng)));
            } else if ("/suggest".equals(path)) {
                int buyerId = Integer.parseInt(req.getParameter("buyerId"));
                int sellerId = Integer.parseInt(req.getParameter("sellerId"));
                out.print(gson.toJson(suggestMeetupLocation(buyerId, sellerId)));
            } else {
                res.setStatus(404);
                out.print("{\"error\":\"Unknown map endpoint\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (!"/transaction-location".equals(req.getPathInfo())) {
            res.setStatus(404);
            out.print("{\"error\":\"Unknown map endpoint\"}");
            return;
        }

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            int transactionId = body.get("transactionId").getAsInt();
            String meetupAddress = body.get("meetupAddress").getAsString();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE transactions SET meetup_address = ? WHERE transaction_id = ?")) {
                stmt.setString(1, meetupAddress);
                stmt.setInt(2, transactionId);
                boolean updated = stmt.executeUpdate() > 0;
                if (!updated) {
                    res.setStatus(404);
                    out.print("{\"error\":\"Transaction not found\"}");
                    return;
                }
            }

            out.print("{\"success\":true,\"message\":\"Meetup location updated\"}");
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private JsonArray getVettedLocations() throws SQLException {
        JsonArray locations = new JsonArray();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT location_id, name, address, latitude, longitude, vetted " +
                     "FROM location WHERE vetted = TRUE ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) locations.add(toLocationJson(rs));
        }
        return locations;
    }

    private JsonObject findClosestLocation(double buyerLat, double buyerLng, double sellerLat, double sellerLng)
            throws SQLException {
        double midLat = (buyerLat + sellerLat) / 2.0;
        double midLng = (buyerLng + sellerLng) / 2.0;

        JsonObject closest = null;
        double closestDistance = Double.MAX_VALUE;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT location_id, name, address, latitude, longitude, vetted FROM location WHERE vetted = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");
                double distance = Math.sqrt(Math.pow(lat - midLat, 2) + Math.pow(lng - midLng, 2));
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = toLocationJson(rs);
                    closest.addProperty("distanceScore", distance);
                }
            }
        }

        if (closest == null) throw new SQLException("No vetted meetup locations found");
        return closest;
    }

    private JsonObject suggestMeetupLocation(int buyerId, int sellerId) throws SQLException {
        JsonObject buyer = getAccountAddress(buyerId);
        JsonObject seller = getAccountAddress(sellerId);
        JsonObject recommended = findClosestToPoint(29.7604, -95.3698);

        JsonObject suggestion = new JsonObject();
        suggestion.add("buyer", buyer);
        suggestion.add("seller", seller);
        suggestion.add("recommendedLocation", recommended);
        suggestion.addProperty(
                "message",
                "Suggested from vetted Houston meetup locations using the buyer and seller addresses saved in the database."
        );
        return suggestion;
    }

    private JsonObject getAccountAddress(int accountId) throws SQLException {
        String sql = "SELECT account_id, username, email, address FROM account WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JsonObject account = new JsonObject();
                account.addProperty("id", rs.getInt("account_id"));
                account.addProperty("name", rs.getString("username"));
                account.addProperty("email", rs.getString("email"));
                account.addProperty("address", rs.getString("address") != null ? rs.getString("address") : "");
                return account;
            }
        }
        throw new SQLException("Account not found");
    }

    private JsonObject findClosestToPoint(double targetLat, double targetLng) throws SQLException {
        JsonObject closest = null;
        double closestDistance = Double.MAX_VALUE;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT location_id, name, address, latitude, longitude, vetted FROM location WHERE vetted = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");
                double distance = Math.sqrt(Math.pow(lat - targetLat, 2) + Math.pow(lng - targetLng, 2));
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = toLocationJson(rs);
                    closest.addProperty("distanceScore", distance);
                }
            }
        }

        if (closest == null) throw new SQLException("No vetted meetup locations found");
        return closest;
    }

    private JsonObject toLocationJson(ResultSet rs) throws SQLException {
        JsonObject location = new JsonObject();
        location.addProperty("id", String.valueOf(rs.getInt("location_id")));
        location.addProperty("name", rs.getString("name"));
        location.addProperty("address", rs.getString("address"));
        location.addProperty("lat", rs.getDouble("latitude"));
        location.addProperty("lng", rs.getDouble("longitude"));
        location.addProperty("type", "Police Station");
        location.addProperty("vetted", rs.getBoolean("vetted"));
        return location;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
