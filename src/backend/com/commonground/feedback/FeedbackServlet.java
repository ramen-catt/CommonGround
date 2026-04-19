package com.commonground.feedback;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/api/feedback/*")
public class FeedbackServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        String sellerId = req.getParameter("sellerId");
        String listingId = req.getParameter("listingId");

        String sql = "SELECT f.feedback_id, f.buyer_id, f.seller_id, f.listing_id, f.rating, " +
                     "f.rating_report, f.rating_desc, f.report_desc, f.created_at, a.username AS reviewer_name " +
                     "FROM feedback f JOIN account a ON f.buyer_id = a.account_id WHERE 1=1 ";
        if (sellerId != null) sql += "AND f.seller_id = ? ";
        if (listingId != null) sql += "AND f.listing_id = ? ";
        sql += "ORDER BY f.created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            if (sellerId != null) stmt.setInt(index++, Integer.parseInt(sellerId));
            if (listingId != null) stmt.setInt(index, Integer.parseInt(listingId));

            ResultSet rs = stmt.executeQuery();
            JsonArray feedback = new JsonArray();
            while (rs.next()) {
                JsonObject item = new JsonObject();
                item.addProperty("id", rs.getInt("feedback_id"));
                item.addProperty("buyerId", rs.getInt("buyer_id"));
                item.addProperty("sellerId", rs.getInt("seller_id"));
                item.addProperty("listingId", rs.getInt("listing_id"));
                item.addProperty("rating", rs.getInt("rating"));
                item.addProperty("isReport", rs.getBoolean("rating_report"));
                item.addProperty("comment", rs.getString("rating_desc"));
                item.addProperty("reportDescription", rs.getString("report_desc"));
                item.addProperty("reviewerName", rs.getString("reviewer_name"));
                item.addProperty("date", rs.getString("created_at"));
                feedback.add(item);
            }
            out.print(gson.toJson(feedback));
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
            int sellerId = body.get("sellerId").getAsInt();
            int listingId = body.get("listingId").getAsInt();
            int rating = body.get("rating").getAsInt();
            String ratingDesc = body.has("ratingDesc") ? body.get("ratingDesc").getAsString() : "";
            String reportDesc = body.has("reportDesc") ? body.get("reportDesc").getAsString() : "";
            boolean isReport = body.has("isReport") && body.get("isReport").getAsBoolean();

            if (rating < 1 || rating > 5) {
                res.setStatus(400);
                out.print("{\"error\":\"Rating must be 1 through 5\"}");
                return;
            }

            String sql = "INSERT INTO feedback (buyer_id, seller_id, listing_id, rating, rating_report, rating_desc, report_desc) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, buyerId);
                stmt.setInt(2, sellerId);
                stmt.setInt(3, listingId);
                stmt.setInt(4, rating);
                stmt.setBoolean(5, isReport);
                stmt.setString(6, ratingDesc);
                stmt.setString(7, reportDesc);
                stmt.executeUpdate();
            }

            out.print("{\"success\":true,\"message\":\"Feedback saved\"}");
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
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
