package com.commonground.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

// handles login, register, logout, and checking who is logged in
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/CommonGround_db";
    private static final String DB_USER = "cguser";
    private static final String DB_PASS = "cgpass123";

    private final Gson gson = new Gson();

    @Override
    public void init() {
        try {
            ensureDemoAccount("tester1", "tester1@gmail.com", "tester1", false, true, true);
            ensureDemoAccount("tester2", "tester2@gmail.com", "tester2", false, true, true);
            ensureDemoAccount("adriana_admin", "adriana_admin@commonground.com", "admin", true, false, false);
            ensureDemoAccount("admin", "admin@commonground.com", "admin", true, false, false);
        } catch (SQLException e) {
            System.err.println("[AuthServlet] Could not prepare demo accounts: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        String path = req.getPathInfo();

        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            out.print("{\"success\":true}");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("accountId") == null) {
            res.setStatus(401);
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }

        JsonObject user = new JsonObject();
        user.addProperty("id",                   (int) session.getAttribute("accountId"));
        user.addProperty("name",                 (String) session.getAttribute("username"));
        user.addProperty("email",                (String) session.getAttribute("email"));
        user.addProperty("isAdmin",              (boolean) session.getAttribute("isAdmin"));
        user.addProperty("address",              session.getAttribute("address") != null
                                                   ? (String) session.getAttribute("address") : "");
        user.addProperty("bannedFromListing",    !(boolean) getOrDefault(session, "canList", true));
        user.addProperty("bannedFromPurchasing", !(boolean) getOrDefault(session, "canPurchase", true));
        out.print(gson.toJson(user));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        String path = req.getPathInfo();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            if ("/login".equals(path)) {
                handleLogin(req, res, out, body);
            } else if ("/register".equals(path)) {
                handleRegister(req, res, out, body);
            } else {
                res.setStatus(404);
                out.print("{\"error\":\"Unknown endpoint\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res,
                              PrintWriter out, JsonObject body) throws SQLException {
        String email    = body.get("email").getAsString();
        String password = body.get("password").getAsString();
        String hashed   = sha256(password);
        String usernameGuess = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        String sql = "SELECT a.account_id, a.username, a.email, a.address, a.password_hash, " +
                     "a.is_admin, a.is_suspended, " +
                     "COALESCE(c.can_list, TRUE) AS can_list, COALESCE(c.can_purchase, TRUE) AS can_purchase " +
                     "FROM account a LEFT JOIN client c ON a.account_id = c.account_id " +
                     "WHERE a.email = ? OR a.username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, usernameGuess);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                if (isDemoPassword(usernameGuess, password)) {
                    ensureDemoAccount(
                            usernameGuess,
                            email,
                            password,
                            "admin".equals(usernameGuess) || "adriana_admin".equals(usernameGuess),
                            !"admin".equals(usernameGuess) && !"adriana_admin".equals(usernameGuess),
                            !"admin".equals(usernameGuess) && !"adriana_admin".equals(usernameGuess));
                    handleLogin(req, res, out, body);
                    return;
                } else {
                    res.setStatus(401);
                    out.print("{\"error\":\"Wrong email or password\"}");
                    return;
                }
            }

            String storedHash = rs.getString("password_hash");
            // accepts SHA-256 hash or legacy plaintext
            if (storedHash == null || (!storedHash.equals(hashed) && !storedHash.equals(password))) {
                if (isDemoPassword(rs.getString("username"), password)) {
                    upgradePlaintextPassword(rs.getInt("account_id"), hashed);
                    tryUpdateDemoEmail(rs.getInt("account_id"), email);
                } else {
                    res.setStatus(401);
                    out.print("{\"error\":\"Wrong email or password\"}");
                    return;
                }
            }

            // upgrade plaintext passwords to SHA-256 on first login
            if (storedHash.equals(password)) {
                upgradePlaintextPassword(rs.getInt("account_id"), hashed);
            }

            if (rs.getBoolean("is_suspended")) {
                res.setStatus(403);
                out.print("{\"error\":\"This account has been suspended\"}");
                return;
            }

            boolean canList     = rs.getBoolean("can_list");
            boolean canPurchase = rs.getBoolean("can_purchase");

            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(60 * 60 * 8);
            session.setAttribute("accountId",   rs.getInt("account_id"));
            session.setAttribute("username",    rs.getString("username"));
            session.setAttribute("email",       rs.getString("email"));
            session.setAttribute("address",     rs.getString("address"));
            session.setAttribute("isAdmin",     rs.getBoolean("is_admin"));
            session.setAttribute("canList",     canList);
            session.setAttribute("canPurchase", canPurchase);

            JsonObject user = new JsonObject();
            user.addProperty("success",              true);
            user.addProperty("id",                   rs.getInt("account_id"));
            user.addProperty("name",                 rs.getString("username"));
            user.addProperty("email",                rs.getString("email"));
            user.addProperty("isAdmin",              rs.getBoolean("is_admin"));
            user.addProperty("address",              rs.getString("address") != null ? rs.getString("address") : "");
            user.addProperty("bannedFromListing",    !canList);
            user.addProperty("bannedFromPurchasing", !canPurchase);
            out.print(gson.toJson(user));
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse res,
                                 PrintWriter out, JsonObject body) throws SQLException {
        String username = body.get("name").getAsString();
        String email    = body.get("email").getAsString();
        String password = body.get("password").getAsString();
        String phone    = body.has("phoneNumber") ? body.get("phoneNumber").getAsString() : "";
        String address  = body.has("address")     ? body.get("address").getAsString()     : "";
        String hashed   = sha256(password);

        String checkSql = "SELECT account_id FROM account WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, email);
            if (check.executeQuery().next()) {
                res.setStatus(409);
                out.print("{\"error\":\"An account with that email already exists\"}");
                return;
            }
        }

        String insertAccount = "INSERT INTO account (username, password_hash, phone_number, address, email) VALUES (?, ?, ?, ?, ?)";
        String insertClient  = "INSERT INTO client (account_id, can_list, can_purchase) VALUES (?, TRUE, TRUE)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement stmt = conn.prepareStatement(insertAccount, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, username);
                    stmt.setString(2, hashed);
                    stmt.setString(3, phone);
                    stmt.setString(4, address);
                    stmt.setString(5, email);
                    stmt.executeUpdate();
                    ResultSet keys = stmt.getGeneratedKeys();
                    keys.next();
                    newId = keys.getInt(1);
                }
                try (PreparedStatement stmt = conn.prepareStatement(insertClient)) {
                    stmt.setInt(1, newId);
                    stmt.executeUpdate();
                }
                conn.commit();

                HttpSession session = req.getSession(true);
                session.setMaxInactiveInterval(60 * 60 * 8);
                session.setAttribute("accountId",   newId);
                session.setAttribute("username",    username);
                session.setAttribute("email",       email);
                session.setAttribute("address",     address);
                session.setAttribute("isAdmin",     false);
                session.setAttribute("canList",     true);
                session.setAttribute("canPurchase", true);

                JsonObject user = new JsonObject();
                user.addProperty("success",              true);
                user.addProperty("id",                   newId);
                user.addProperty("name",                 username);
                user.addProperty("email",                email);
                user.addProperty("isAdmin",              false);
                user.addProperty("address",              address);
                user.addProperty("bannedFromListing",    false);
                user.addProperty("bannedFromPurchasing", false);
                res.setStatus(201);
                out.print(gson.toJson(user));

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void upgradePlaintextPassword(int accountId, String hashedPassword) throws SQLException {
        String sql = "UPDATE account SET password_hash = ? WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    private boolean isDemoPassword(String username, String password) {
        return ("tester1".equals(username) && "tester1".equals(password)) ||
               ("tester2".equals(username) && "tester2".equals(password)) ||
               ("admin".equals(username) && "admin".equals(password)) ||
               ("adriana_admin".equals(username) && "admin".equals(password));
    }

    private void tryUpdateDemoEmail(int accountId, String email) {
        if (email == null || !email.contains("@")) return;
        String sql = "UPDATE account SET email = ? WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuthServlet] Could not update demo email: " + e.getMessage());
        }
    }

    private void ensureDemoAccount(String username, String email, String password,
                                   boolean isAdmin, boolean canList, boolean canPurchase) throws SQLException {
        String hashed = sha256(password);
        Integer accountId = null;

        try (Connection conn = getConnection();
             PreparedStatement find = conn.prepareStatement(
                     "SELECT account_id FROM account WHERE username = ? OR email = ? LIMIT 1")) {
            find.setString(1, username);
            find.setString(2, email);
            ResultSet rs = find.executeQuery();
            if (rs.next()) {
                accountId = rs.getInt("account_id");
            }
        }

        if (accountId == null) {
            try (Connection conn = getConnection();
                 PreparedStatement insert = conn.prepareStatement(
                         "INSERT INTO account (username, password_hash, phone_number, address, email, is_admin, is_suspended) " +
                         "VALUES (?, ?, ?, ?, ?, ?, FALSE)",
                         Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, username);
                insert.setString(2, hashed);
                insert.setString(3, "5550000000");
                insert.setString(4, "Houston, TX");
                insert.setString(5, email);
                insert.setBoolean(6, isAdmin);
                insert.executeUpdate();
                ResultSet keys = insert.getGeneratedKeys();
                keys.next();
                accountId = keys.getInt(1);
            }
        } else {
            try (Connection conn = getConnection();
                 PreparedStatement update = conn.prepareStatement(
                         "UPDATE account SET email = ?, password_hash = ?, is_admin = ?, is_suspended = FALSE WHERE account_id = ?")) {
                update.setString(1, email);
                update.setString(2, hashed);
                update.setBoolean(3, isAdmin);
                update.setInt(4, accountId);
                update.executeUpdate();
            }
        }

        if (!isAdmin) {
            try (Connection conn = getConnection();
                 PreparedStatement client = conn.prepareStatement(
                         "INSERT INTO client (account_id, can_list, can_purchase) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE can_list = VALUES(can_list), can_purchase = VALUES(can_purchase)")) {
                client.setInt(1, accountId);
                client.setBoolean(2, canList);
                client.setBoolean(3, canPurchase);
                client.executeUpdate();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // SHA-256 password hashing
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return input;
        }
    }

    private static Object getOrDefault(HttpSession session, String key, Object def) {
        Object val = session.getAttribute(key);
        return val != null ? val : def;
    }
}
