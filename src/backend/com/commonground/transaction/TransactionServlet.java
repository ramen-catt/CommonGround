package com.commonground.transaction;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/api/transactions/*")
public class TransactionServlet extends HttpServlet {
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_BUYER_CONFIRMED = "Buyer Confirmed";
    private static final String STATUS_SELLER_CONFIRMED = "Seller Confirmed";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_CANCELLED = "Cancelled";

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        Integer accountId = getAccountId(req);
        if (accountId == null) {
            res.setStatus(401);
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }

        try (Connection conn = getConnection()) {
            String id = req.getParameter("id");
            if (id != null) {
                JsonObject transaction = getTransaction(conn, Integer.parseInt(id), accountId);
                if (transaction == null) {
                    res.setStatus(404);
                    out.print("{\"error\":\"Transaction not found\"}");
                } else {
                    out.print(gson.toJson(transaction));
                }
            } else {
                out.print(gson.toJson(getTransactionsForUser(conn, accountId)));
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
        Integer buyerId = getAccountId(req);
        if (buyerId == null) {
            res.setStatus(401);
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            int listingId = body.get("listingId").getAsInt();
            int sellerId = body.get("sellerId").getAsInt();
            String meetupAddress = body.has("meetupAddress") ? body.get("meetupAddress").getAsString() : "";

            try (Connection conn = getConnection()) {
                if (sellerId == buyerId) {
                    res.setStatus(400);
                    out.print("{\"error\":\"You cannot buy your own listing\"}");
                    return;
                }

                int transactionId;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO transactions (buyer_id, seller_id, listing_id, meetup_address, status) " +
                        "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, buyerId);
                    stmt.setInt(2, sellerId);
                    stmt.setInt(3, listingId);
                    stmt.setString(4, meetupAddress);
                    stmt.setString(5, STATUS_PENDING);
                    stmt.executeUpdate();
                    ResultSet keys = stmt.getGeneratedKeys();
                    keys.next();
                    transactionId = keys.getInt(1);
                }

                logAuditAsync(transactionId, listingId, buyerId, "CreateTransaction", STATUS_PENDING);

                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("id", transactionId);
                response.add("transaction", getTransaction(conn, transactionId, buyerId));
                res.setStatus(201);
                out.print(gson.toJson(response));
            }
        } catch (Exception e) {
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
            int transactionId = body.get("id").getAsInt();
            String action = body.get("action").getAsString();

            try (Connection conn = getConnection()) {
                JsonObject transaction = getTransaction(conn, transactionId, accountId);
                if (transaction == null) {
                    res.setStatus(404);
                    out.print("{\"error\":\"Transaction not found\"}");
                    return;
                }

                String newStatus;
                if ("cancel".equals(action)) {
                    newStatus = STATUS_CANCELLED;
                } else if ("confirm".equals(action)) {
                    boolean isBuyer = transaction.get("buyerId").getAsInt() == accountId;
                    String current = transaction.get("status").getAsString();
                    if (STATUS_PENDING.equals(current)) {
                        newStatus = isBuyer ? STATUS_BUYER_CONFIRMED : STATUS_SELLER_CONFIRMED;
                    } else if ((isBuyer && STATUS_SELLER_CONFIRMED.equals(current)) ||
                               (!isBuyer && STATUS_BUYER_CONFIRMED.equals(current))) {
                        newStatus = STATUS_COMPLETED;
                    } else {
                        newStatus = current;
                    }
                } else {
                    res.setStatus(400);
                    out.print("{\"error\":\"Unknown action\"}");
                    return;
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE transactions SET status = ? WHERE transaction_id = ?")) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, transactionId);
                    stmt.executeUpdate();
                }

                int listingId = transaction.get("listingId").getAsInt();

                if (STATUS_COMPLETED.equals(newStatus)) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE listing SET status = 'Sold' WHERE listing_id = ?")) {
                        stmt.setInt(1, listingId);
                        stmt.executeUpdate();
                    }
                }

                logAuditAsync(transactionId, listingId, accountId, "TransactionStatusChange", newStatus);

                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.add("transaction", getTransaction(conn, transactionId, accountId));
                out.print(gson.toJson(response));
            }
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private JsonArray getTransactionsForUser(Connection conn, int accountId) throws SQLException {
        String sql = baseSql() + "WHERE t.buyer_id = ? OR t.seller_id = ? ORDER BY t.transaction_time DESC";
        JsonArray transactions = new JsonArray();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) transactions.add(toJson(rs, accountId));
        }
        return transactions;
    }

    private JsonObject getTransaction(Connection conn, int transactionId, int accountId) throws SQLException {
        String sql = baseSql() + "WHERE t.transaction_id = ? AND (t.buyer_id = ? OR t.seller_id = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return toJson(rs, accountId);
        }
        return null;
    }

    private String baseSql() {
        return "SELECT t.*, l.item_name, l.price, l.payment_type, " +
               "loc.name AS meetup_name, loc.latitude AS meetup_lat, loc.longitude AS meetup_lng, " +
               "ba.username AS buyer_name, ba.email AS buyer_email, " +
               "sa.username AS seller_name, sa.email AS seller_email " +
               "FROM transactions t " +
               "JOIN listing l ON t.listing_id = l.listing_id " +
               "JOIN account ba ON t.buyer_id = ba.account_id " +
               "JOIN account sa ON t.seller_id = sa.account_id " +
               "LEFT JOIN location loc ON t.meetup_address = loc.address ";
    }

    private JsonObject toJson(ResultSet rs, int accountId) throws SQLException {
        String status = rs.getString("status");
        boolean buyerConfirmed = STATUS_BUYER_CONFIRMED.equals(status) || STATUS_COMPLETED.equals(status);
        boolean sellerConfirmed = STATUS_SELLER_CONFIRMED.equals(status) || STATUS_COMPLETED.equals(status);

        JsonObject meetup = new JsonObject();
        meetup.addProperty("name", rs.getString("meetup_name") != null ? rs.getString("meetup_name") : rs.getString("meetup_address"));
        meetup.addProperty("address", rs.getString("meetup_address"));
        meetup.addProperty("lat", rs.getDouble("meetup_lat"));
        meetup.addProperty("lng", rs.getDouble("meetup_lng"));

        JsonObject obj = new JsonObject();
        obj.addProperty("id", String.valueOf(rs.getInt("transaction_id")));
        obj.addProperty("buyerId", rs.getInt("buyer_id"));
        obj.addProperty("sellerId", rs.getInt("seller_id"));
        obj.addProperty("listingId", rs.getInt("listing_id"));
        obj.addProperty("buyerName", rs.getString("buyer_name"));
        obj.addProperty("buyerEmail", rs.getString("buyer_email"));
        obj.addProperty("sellerName", rs.getString("seller_name"));
        obj.addProperty("sellerEmail", rs.getString("seller_email"));
        obj.addProperty("listingTitle", rs.getString("item_name"));
        obj.addProperty("agreedPrice", rs.getDouble("price"));
        obj.addProperty("paymentMethod", rs.getString("payment_type") != null ? rs.getString("payment_type") : "Cash");
        obj.add("meetupLocation", meetup);
        obj.addProperty("status", status);
        obj.addProperty("buyerConfirmed", buyerConfirmed);
        obj.addProperty("sellerConfirmed", sellerConfirmed);
        obj.addProperty("timestamp", rs.getString("transaction_time"));
        obj.addProperty("userId", String.valueOf(accountId));
        return obj;
    }

    private Integer getAccountId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("accountId") == null) return null;
        return (Integer) session.getAttribute("accountId");
    }

    private Connection getConnection() throws SQLException {
        return com.commonground.util.DbUtil.getConnection();
    }

    private void logAuditAsync(int transactionId, int listingId, int clientId, String action, String newStatus) {
        Thread t = new Thread(() -> {
            String sql = "INSERT INTO audit_trail (transaction_id, listing_id, client_id, action, new_status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, transactionId);
                stmt.setInt(2, listingId);
                stmt.setInt(3, clientId);
                stmt.setString(4, action);
                stmt.setString(5, newStatus);
                stmt.executeUpdate();
                System.out.println("[TransactionServlet] Audit record written: " + action + " | TxID=" + transactionId + " | Status=" + newStatus);
            } catch (Exception e) {
                System.err.println("[TransactionServlet] Audit write failed: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
