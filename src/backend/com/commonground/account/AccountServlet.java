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

        try {
            ensureAccountDeletionColumn();
        } catch (SQLException e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.account_id, a.username, a.email, a.phone_number, a.address, a.created_at, " +
                     "COALESCE(AVG(f.rating), 5) AS rating, COUNT(f.feedback_id) AS total_reviews " +
                     "FROM account a " +
                     "LEFT JOIN feedback f ON a.account_id = f.seller_id AND f.rating_report = FALSE " +
                     "WHERE a.email = ? AND a.is_deleted = FALSE " +
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
                ensureAccountDeletionColumn(conn);
                JsonObject deletedAccount = getAccountForDeletion(conn, accountId);
                if (deletedAccount == null) {
                    res.setStatus(404);
                    out.print("{\"error\":\"Account not found\"}");
                    conn.rollback();
                    return;
                }

                ensureDeletedAccountTable(conn);
                rememberDeletedAccount(conn,
                        deletedAccount.get("email").getAsString(),
                        deletedAccount.get("username").getAsString());
                markAccountDeleted(conn, accountId);

                // Fully wipe this account and its data. The deleted_account row stays
                // behind so the same email/username cannot log in or be recreated.
                conn.prepareStatement("SET foreign_key_checks = 0").execute();
                exec(conn, "DELETE FROM feedback WHERE buyer_id = ? OR seller_id = ?", accountId, accountId);
                exec(conn, "DELETE FROM audit_trail WHERE client_id = ?", accountId);
                exec(conn, "DELETE FROM transactions WHERE buyer_id = ? OR seller_id = ?", accountId, accountId);
                exec(conn, "DELETE FROM message WHERE sender_id = ? OR receiver_id = ?", accountId, accountId);
                exec(conn, "DELETE FROM listing_image WHERE listing_id IN (SELECT listing_id FROM listing WHERE client_id = ?)", accountId);
                exec(conn, "DELETE FROM listing WHERE client_id = ?", accountId);
                exec(conn, "DELETE FROM client WHERE account_id = ?", accountId);
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

    private JsonObject getAccountForDeletion(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT username, email FROM account WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            JsonObject account = new JsonObject();
            account.addProperty("username", rs.getString("username"));
            account.addProperty("email", rs.getString("email"));
            return account;
        }
    }

    private void ensureDeletedAccountTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS deleted_account (" +
                     "deleted_account_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "email VARCHAR(100) NOT NULL UNIQUE, " +
                     "username VARCHAR(50) NOT NULL UNIQUE, " +
                     "deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    private void rememberDeletedAccount(Connection conn, String email, String username) throws SQLException {
        String sql = "INSERT INTO deleted_account (email, username) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE deleted_at = CURRENT_TIMESTAMP";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }

    private void markAccountDeleted(Connection conn, int accountId) throws SQLException {
        String sql = "UPDATE account " +
                     "SET is_deleted = TRUE, is_suspended = TRUE, password_hash = ?, phone_number = '', address = '' " +
                     "WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "deleted-" + accountId + "-" + System.nanoTime());
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    private void ensureAccountDeletionColumn() throws SQLException {
        if (hasColumn("account", "is_deleted")) return;
        try (Connection conn = getConnection()) {
            ensureAccountDeletionColumn(conn);
        }
    }

    private void ensureAccountDeletionColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "account", "is_deleted")) return;
        try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE account ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE")) {
            stmt.executeUpdate();
        }
    }

    private boolean hasColumn(String tableName, String columnName) throws SQLException {
        try (Connection conn = getConnection()) {
            return hasColumn(conn, tableName, columnName);
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return columns.next();
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
