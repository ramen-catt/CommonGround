package com.commonground.admin;

// This talks to database 
// Miguel V make sure to implement these in the code below 
// 1. SETTING AN ADMIN ACCOUNT:
//    When you create an admin account in your AccountDAO, run this:
//    "UPDATE account SET is_admin = TRUE WHERE account_id = ?"
//    My code checks that column automatically before allowing
//    any admin action. 
//
// 2. CHECKING SUSPENSION ON LOGIN:
//    When a user logs in, check is_suspended before letting them in:
//    "SELECT is_suspended FROM account WHERE account_id = ?"
//    If is_suspended = TRUE → block the login and show an error.
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    // same credentials as ListingDAO
    private Connection getConnection() throws SQLException {
        return com.commonground.util.DbUtil.getConnection();
    }


  // checks if they are an admin 

    public boolean isAdmin(int accountId) throws SQLException {

        String sql = "SELECT is_admin FROM account WHERE account_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_admin");
            }
        }
        return false; // if account is not found then they are not an admin 
    }


 //suspends someone, admin cannot suspend another admin 

    public boolean setSuspended(int targetAccountId, boolean suspended) throws SQLException {

        // block suspending another admin
        String checkSql = "SELECT is_admin FROM account WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {

            check.setInt(1, targetAccountId);
            ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getBoolean("is_admin")) {
                return false; // cannot suspend another admin
            }
        }

        String sql = "UPDATE account SET is_suspended = ? WHERE account_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, suspended);
            stmt.setInt    (2, targetAccountId);

            return stmt.executeUpdate() > 0;
        }
    }


   //removing listing, such as a reported fraud one, admin can remove any listing even sold ones 

    public boolean adminRemoveListing(int listingId) throws SQLException {

        String sql = "UPDATE listing SET status = 'Removed' WHERE listing_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listingId);
            return stmt.executeUpdate() > 0;
        }
    }

//see the reports 

    public List<String[]> getAllReports() throws SQLException {

        String sql = "SELECT feedback_id, buyer_id, seller_id, listing_id, " +
                     "rating_desc, report_desc, created_at " +
                     "FROM feedback " +
                     "WHERE rating_report = TRUE " +
                     "ORDER BY created_at DESC";

        List<String[]> reports = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(new String[]{
                    String.valueOf(rs.getInt("feedback_id")),
                    String.valueOf(rs.getInt("buyer_id")),
                    String.valueOf(rs.getInt("seller_id")),
                    String.valueOf(rs.getInt("listing_id")),
                    rs.getString("rating_desc"),
                    rs.getString("report_desc"),
                    rs.getString("created_at")
                });
            }
        }
        return reports;
    }


   //removes the review 

    public boolean removeReview(int feedbackId) throws SQLException {

        String sql = "DELETE FROM feedback WHERE feedback_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedbackId);
            return stmt.executeUpdate() > 0;
        }
    }


   //look at accounts 

    public List<String[]> getAllAccounts() throws SQLException {

        String sql = "SELECT account_id, username, email, is_admin, is_suspended " +
                     "FROM account ORDER BY account_id ASC";

        List<String[]> accounts = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accounts.add(new String[]{
                    String.valueOf(rs.getInt("account_id")),
                    rs.getString("username"),
                    rs.getString("email"),
                    String.valueOf(rs.getBoolean("is_admin")),
                    String.valueOf(rs.getBoolean("is_suspended"))
                });
            }
        }
        return accounts;
    }


  //views all transactions 

    public List<String[]> getAllTransactions() throws SQLException {

        String sql = "SELECT transaction_id, buyer_id, seller_id, listing_id, " +
                     "status, transaction_time " +
                     "FROM transactions ORDER BY transaction_time DESC";

        List<String[]> transactions = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(new String[]{
                    String.valueOf(rs.getInt("transaction_id")),
                    String.valueOf(rs.getInt("buyer_id")),
                    String.valueOf(rs.getInt("seller_id")),
                    String.valueOf(rs.getInt("listing_id")),
                    rs.getString("status"),
                    rs.getString("transaction_time")
                });
            }
        }
        return transactions;
    }
}
