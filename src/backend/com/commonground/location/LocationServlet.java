package com.commonground.location;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/api/locations")
public class LocationServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CommonGround_db";
    private static final String DB_USER = "cguser";
    private static final String DB_PASS = "cgpass123";

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        JsonArray locations = new JsonArray();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT location_id, name, address, latitude, longitude, vetted " +
                     "FROM location WHERE vetted = TRUE ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                JsonObject location = new JsonObject();
                location.addProperty("id", String.valueOf(rs.getInt("location_id")));
                location.addProperty("name", rs.getString("name"));
                location.addProperty("address", rs.getString("address"));
                location.addProperty("lat", rs.getDouble("latitude"));
                location.addProperty("lng", rs.getDouble("longitude"));
                location.addProperty("type", "Police Station");
                location.addProperty("vetted", rs.getBoolean("vetted"));
                locations.add(location);
            }
            out.print(gson.toJson(locations));
        } catch (SQLException e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
