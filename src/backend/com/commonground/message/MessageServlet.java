package com.commonground.message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/messages/*")
public class MessageServlet extends HttpServlet {
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
            boolean hasListingId = ensureListingIdColumn(conn);
            if ("/conversation".equals(req.getPathInfo())) {
                int otherId = Integer.parseInt(req.getParameter("otherId"));
                Integer listingId = parseOptionalInt(req.getParameter("listingId"));
                out.print(gson.toJson(getMessages(conn, accountId, otherId, listingId, hasListingId)));
            } else {
                out.print(gson.toJson(getConversations(conn, accountId, hasListingId)));
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

        Integer senderId = getAccountId(req);
        if (senderId == null) {
            res.setStatus(401);
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            int receiverId;
            if (body.has("receiverId") && !body.get("receiverId").isJsonNull()) {
                receiverId = body.get("receiverId").getAsInt();
            } else {
                receiverId = findAccountIdByEmail(body.get("receiverEmail").getAsString());
            }

            Integer listingId = body.has("listingId") && !body.get("listingId").isJsonNull()
                    ? body.get("listingId").getAsInt()
                    : null;
            String text = body.get("text").getAsString().trim();
            if (text.isBlank()) {
                res.setStatus(400);
                out.print("{\"error\":\"Message cannot be empty\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                boolean hasListingId = ensureListingIdColumn(conn);
                String sql = hasListingId
                        ? "INSERT INTO message (sender_id, receiver_id, listing_id, message_text) VALUES (?, ?, ?, ?)"
                        : "INSERT INTO message (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, senderId);
                    stmt.setInt(2, receiverId);
                    if (hasListingId) {
                        if (listingId == null) stmt.setNull(3, Types.INTEGER);
                        else stmt.setInt(3, listingId);
                        stmt.setString(4, text);
                    } else {
                        stmt.setString(3, text);
                    }
                    stmt.executeUpdate();
                }
            }

            out.print("{\"success\":true,\"message\":\"Message sent\"}");
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private JsonArray getConversations(Connection conn, int accountId, boolean hasListingId) throws SQLException {
        String sql = hasListingId
                ? "SELECT m.*, " +
                  "sa.username AS sender_name, sa.email AS sender_email, " +
                  "ra.username AS receiver_name, ra.email AS receiver_email, " +
                  "l.listing_id, l.item_name AS listing_title, l.client_id AS seller_id, " +
                  "seller.username AS seller_name, seller.email AS seller_email " +
                  "FROM message m " +
                  "JOIN account sa ON m.sender_id = sa.account_id " +
                  "JOIN account ra ON m.receiver_id = ra.account_id " +
                  "LEFT JOIN listing l ON m.listing_id = l.listing_id " +
                  "LEFT JOIN account seller ON l.client_id = seller.account_id " +
                  "WHERE m.sender_id = ? OR m.receiver_id = ? " +
                  "ORDER BY m.sent_at ASC"
                : "SELECT m.*, " +
                  "sa.username AS sender_name, sa.email AS sender_email, " +
                  "ra.username AS receiver_name, ra.email AS receiver_email, " +
                  "NULL AS listing_id, NULL AS listing_title, NULL AS seller_id, " +
                  "NULL AS seller_name, NULL AS seller_email " +
                  "FROM message m " +
                  "JOIN account sa ON m.sender_id = sa.account_id " +
                  "JOIN account ra ON m.receiver_id = ra.account_id " +
                  "WHERE m.sender_id = ? OR m.receiver_id = ? " +
                  "ORDER BY m.sent_at ASC";
        Map<String, JsonObject> conversations = new LinkedHashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                int otherId = senderId == accountId ? receiverId : senderId;
                String otherName = senderId == accountId ? rs.getString("receiver_name") : rs.getString("sender_name");
                String otherEmail = senderId == accountId ? rs.getString("receiver_email") : rs.getString("sender_email");
                Integer listingId = getNullableInt(rs, "listing_id");
                Integer sellerId = getNullableInt(rs, "seller_id");
                String key = otherId + ":" + (listingId == null ? "general" : listingId);

                JsonObject conversation = conversations.get(key);
                if (conversation == null) {
                    conversation = new JsonObject();
                    conversation.addProperty("id", key);
                    conversation.addProperty("otherId", otherId);
                    conversation.addProperty("otherName", otherName);
                    conversation.addProperty("otherEmail", otherEmail);
                    if (listingId != null) conversation.addProperty("listingId", listingId);
                    if (sellerId != null) conversation.addProperty("sellerId", sellerId);
                    conversation.addProperty("sellerName",
                            rs.getString("seller_name") != null ? rs.getString("seller_name") : otherName);
                    conversation.addProperty("sellerEmail",
                            rs.getString("seller_email") != null ? rs.getString("seller_email") : otherEmail);
                    conversation.addProperty("listingTitle",
                            rs.getString("listing_title") != null ? rs.getString("listing_title") : "Conversation");
                    conversation.add("messages", new JsonArray());
                    conversations.put(key, conversation);
                }
                conversation.getAsJsonArray("messages").add(toMessageJson(rs, accountId));
                conversation.addProperty("lastMessageTime", rs.getString("sent_at"));
            }
        }

        JsonArray array = new JsonArray();
        for (JsonObject conversation : conversations.values()) array.add(conversation);
        return array;
    }

    private JsonArray getMessages(Connection conn, int accountId, int otherId, Integer listingId, boolean hasListingId)
            throws SQLException {
        String sql = hasListingId && listingId != null
                ? "SELECT m.*, sa.username AS sender_name, sa.email AS sender_email " +
                  "FROM message m JOIN account sa ON m.sender_id = sa.account_id " +
                  "WHERE (((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) " +
                  "AND m.listing_id = ?) ORDER BY m.sent_at ASC"
                : "SELECT m.*, sa.username AS sender_name, sa.email AS sender_email " +
                  "FROM message m JOIN account sa ON m.sender_id = sa.account_id " +
                  "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                  "ORDER BY m.sent_at ASC";
        JsonArray messages = new JsonArray();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, otherId);
            stmt.setInt(3, otherId);
            stmt.setInt(4, accountId);
            if (hasListingId && listingId != null) stmt.setInt(5, listingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) messages.add(toMessageJson(rs, accountId));
        }
        return messages;
    }

    private JsonObject toMessageJson(ResultSet rs, int accountId) throws SQLException {
        JsonObject message = new JsonObject();
        message.addProperty("id", String.valueOf(rs.getInt("message_id")));
        message.addProperty("text", rs.getString("message_text"));
        message.addProperty("senderId", rs.getInt("sender_id"));
        message.addProperty("senderName", rs.getString("sender_name"));
        message.addProperty("senderEmail", rs.getString("sender_email"));
        message.addProperty("timestamp", rs.getString("sent_at"));
        message.addProperty("isMine", rs.getInt("sender_id") == accountId);
        message.add("readBy", new JsonArray());
        return message;
    }

    private boolean ensureListingIdColumn(Connection conn) {
        try {
            if (hasColumn(conn, "message", "listing_id")) return true;
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE message ADD COLUMN listing_id INT NULL AFTER receiver_id");
            }
            return hasColumn(conn, "message", "listing_id");
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value);
    }

    private int findAccountIdByEmail(String email) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT account_id FROM account WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("account_id");
        }
        throw new SQLException("Receiver account not found");
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
