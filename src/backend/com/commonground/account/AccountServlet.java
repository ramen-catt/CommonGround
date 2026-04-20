package com.commonground.account;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/api/account/*")
public class AccountServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        String email = req.getParameter("email");
        if (email == null || email.isBlank()) {
            res.setStatus(400);
            out.print("{\"error\":\"Missing email\"}");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.account_id, a.username, a.email, a.phone_number, a.address, a.created_at, " +
                     "COALESCE(AVG(f.rating), 5) AS rating, COUNT(f.feedback_id) AS total_reviews " +
                     "FROM account a " +
                     "LEFT JOIN feedback f ON a.account_id = f.seller_id AND f.rating_report = FALSE " +
                     "WHERE a.email = ? " +
                     "GROUP BY a.account_id, a.username, a.email, a.phone_number, a.address, a.created_at")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                res.setStatus(404);
                out.print("{\"error\":\"User not found\"}");
                return;
            }

            int profileAccountId = rs.getInt("account_id");
            // only show private fields (phone, address) to the account owner
            Integer sessionAccountId = getAccountId(req);
            boolean isOwner = sessionAccountId != null && sessionAccountId == profileAccountId;

            JsonObject profile = new JsonObject();
            profile.addProperty("id", profileAccountId);
            profile.addProperty("name", rs.getString("username"));
            profile.addProperty("email", rs.getString("email"));
            profile.addProperty("rating", rs.getDouble("rating"));
            profile.addProperty("totalReviews", rs.getInt("total_reviews"));
            profile.addProperty("location", "Houston, TX"); // city-level only — no home address exposed
            Timestamp createdAt = rs.getTimestamp("created_at");
            profile.addProperty("joinDate", createdAt != null
                    ? new java.text.SimpleDateFormat("MMMM yyyy").format(createdAt)
                    : "N/A");
            // private fields only returned to the owner
            if (isOwner) {
                profile.addProperty("phoneNumber", rs.getString("phone_number") != null ? rs.getString("phone_number") : "");
                profile.addProperty("address", rs.getString("address") != null ? rs.getString("address") : "");
            }
            out.print(gson.toJson(profile));
        } catch (SQLException e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        Integer accountId = getAccountId(req);
        if (accountId == null) {
            res.setStatus(401);
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            String name = body.get("name").getAsString().trim();
            String phone = body.has("phoneNumber") ? body.get("phoneNumber").getAsString().trim() : "";
            String address = body.has("address") ? body.get("address").getAsString().trim() : "";

            if (name.isBlank()) {
                res.setStatus(400);
                out.print("{\"error\":\"Name is required\"}");
                return;
            }

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE account SET username = ?, phone_number = ?, address = ? WHERE account_id = ?")) {
                stmt.setString(1, name);
                stmt.setString(2, phone);
                stmt.setString(3, address);
                stmt.setInt(4, accountId);
                stmt.executeUpdate();
            }

            HttpSession session = req.getSession(false);
            if (session != null) {
                session.setAttribute("username", name);
                session.setAttribute("address", address);
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("name", name);
            response.addProperty("phoneNumber", phone);
            response.addProperty("address", address);
            out.print(gson.toJson(response));
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        Integer accountId = getAccountId(req);
        if (accountId == null) {
            res.setStatus(401);
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Disable FK checks so we don't have to know every junction table
                conn.prepareStatement("SET foreign_key_checks = 0").execute();
                exec(conn, "DELETE FROM feedback WHERE buyer_id = ? OR seller_id = ?", accountId, accountId);
                exec(conn, "DELETE FROM audit_trail WHERE client_id = ?", accountId);
                exec(conn, "DELETE FROM transactions WHERE buyer_id = ? OR seller_id = ?", accountId, accountId);
                exec(conn, "DELETE FROM message WHERE sender_id = ? OR receiver_id = ?", accountId, accountId);
                exec(conn, "DELETE FROM listing_image WHERE listing_id IN (SELECT listing_id FROM listing WHERE client_id = ?)", accountId);
                exec(conn, "DELETE FROM listing WHERE client_id = ?", accountId);
                exec(conn, "DELETE FROM account WHERE account_id = ?", accountId);
                conn.prepareStatement("SET foreign_key_checks = 1").execute();
                conn.commit();
            } catch (SQLException e) {
                conn.prepareStatement("SET foreign_key_checks = 1").execute();
                conn.rollback();
                throw e;
            }

            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();

            out.print("{\"success\":true}");
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void exec(Connection conn, String sql, int... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setInt(i + 1, params[i]);
            stmt.executeUpdate();
        }
    }

    private Integer getAccountId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("accountId") == null) return null;
        return (Integer) session.getAttribute("accountId");
    }

    private Connection getConnection() throws SQLException {
        return com.commonground.util.DbUtil.getConnection();
    }
}
