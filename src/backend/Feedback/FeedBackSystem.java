package com.commonground.feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FeedBackSystem {

    private static final Scanner sc = new Scanner(System.in);
    // Connect to the Database
    private static final String url = "jdbc:mysql://localhost:3306/CommonGround_db";
    private static final String user = "cguser";
    private static final String pass = "cgpass123";

    public static void main(String[] args) {
        System.out.print("====FeedBack Collection System====");
        // All the diffrent options
        while (true) {
            System.out.println("\n1. Submit Review");
            System.out.println("2. Submit Report");
            System.out.println("3. View All Feedback");
            System.out.println("4. View Listing Stats");
            System.out.println("5. View Average Rating"); 
            System.out.println("6. View Average Rating"); 
            System.out.println("7. View Review Count");
            System.out.println("8. View Report Count");
            System.out.println("9. Remove Review");
            System.out.println("10. Remove Report");
            System.out.println("11. Exit");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    submitReview();
                    break;
                case "2":
                    submitReport(); 
                    break;
                case "3":
                    viewAllFeedbackForListing();
                    break;
                case "4":
                    viewListingStats(); 
                    break;
                case "5":
                    showAverageRatingAll();
                    break;
                case "6":
                    showAverageRatingReviewsOnly();
                    break;
                case "7":
                    showReviewCount();
                    break;
                case "8":
                    showReportCount();
                    break;
                case "9":
                    removeReviewMenu();
                    break;
                case "10":
                    removeReportMenu();
                    break;
                case "11":
                    System.out.println("Exiting... Thank you!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again");
            }
        }
    }
    // Get input
    public static int getIntInput(String prompt) {
        while (true){
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()){
                continue;
            }
            try{
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again");
            }
        }
    }
    // Get string
    private static String getStringInput(String prompt){
        System.out.println(prompt);
        return sc.nextLine().trim();
    }
    // Submit a review
    private static void submitReview() {
        System.out.println("\n=== Submit Review ===");
        int buyerId = getIntInput("Enter Buyer ID: ");
        int sellerId = getIntInput("Enter Seller ID: ");
        int listingId = getIntInput("Enter Listing ID: ");
        int rating = getIntInput("Enter rating (1-5): ");
        String ratingDesc = getStringInput("Enter short review description: ");

        FeedBack fb = new FeedBack(buyerId, sellerId, listingId, rating, ratingDesc);
        addReview(fb);

        System.out.println("Review submitted");
    }
    // Submit a report
    private static void submitReport() {
        System.out.println("\n === Submit Report ===");
        int buyerId = getIntInput("Enter Buyer ID: ");
        int sellerId = getIntInput("Enter Seller ID: ");
        int listingId = getIntInput("Enter Listing ID: ");
        int rating = getIntInput("Enter rating (1-5) for report: ");
        String ratingDesc = getStringInput("Enter short rating: ");
        String reportDesc = getStringInput("Enter report description: ");

        FeedBack fb = new FeedBack(buyerId,sellerId, listingId, rating, ratingDesc, reportDesc);
        addReport(fb);

        System.out.println("Report submitted");
    }
    // View the feedback for a certain listing ID 
    private static void viewAllFeedbackForListing(){
        System.out.println("\n === View All Feedback for Listing ===");
        int listingId = getIntInput("Enter Listing ID: "); 
        List<FeedBack> feedbackList = getFeedbackForListing(listingId); 
        if (feedbackList.isEmpty()){
            System.out.println("No feedback found for this listing");
            return;
        }
        for (FeedBack fb : feedbackList){
            System.out.println(fb);
        }
    }
    // View the listing stats for a certain listing ID (Avg rating, total review (reviews only), total review, total report)
    private static void viewListingStats(){
        System.out.println("\n === Listing Stats ===");
        int listingId = getIntInput("Enter Listing ID: ");
        double avgAll = getAverageRatingAll(listingId);
        double avgReviews = getReviewAverage(listingId);
        int reviewCount = getReviewCountDB(listingId); 
        int reportCount = getReportCount(listingId);

        System.out.println("Listing ID: " + listingId);
        System.out.println("Average Rating (All feedback): " + avgAll);
        System.out.println("Total Reviews (Reviews only): " + avgReviews);
        System.out.println("Total Reviews: " + reviewCount);
        System.out.println("Total Reports: " + reportCount);
        System.out.println("===============\n");
    }
    // Gives average for certain listing 
    private static void showAverageRatingAll() {
        System.out.println("\n === Average Rating (All Feedback) ===");
        int listingId = getIntInput("Enter Listing ID: ");
        double avg = getAverageRatingAll(listingId);
        if (avg == 0) {
            System.out.println("No rating for this user.");
        } else {
            System.out.println("Average Rating: " + avg);
        }
    }
    // Gives average for the reviews only
    private static void showAverageRatingReviewsOnly() {
        System.out.println("\n === Average Rating (Reviews Only) ===");
        int listingId = getIntInput("Enter Listing ID: "); 
        double avg = getReviewAverage(listingId);
        if (avg == 0) {
            System.out.println("No rating for this user.");
        } else {
            System.out.println("Average Rating (Reviews only): " + avg);
        }
    }
    // Show the amount of reviews a listing has
    private static void showReviewCount() {
        System.out.println("\n === Review Count ===");
        int listingId = getIntInput("Enter Listing ID: ");
        int count = getReviewCountDB(listingId);
        System.out.println("Total Reviews: " + count);
    }
    // Show the amount of report a listing has
    private static void showReportCount() {
        System.out.println("\n === Report Count ===");
        int listingId = getIntInput("Enter Listing ID: ");
        int count = getReportCount(listingId);
        System.out.println("Total Reports: " + count);
    }
    // Remove a review
    private static void removeReviewMenu(){
        System.out.println("\n=== Remove Review ===");
        int feedbackId = getIntInput("Enter Feedback ID to remove (Review): ");
        removeReview(feedbackId);
        System.out.println("If a review with that ID existed, it has been removed");
    }
    // Remove a report
    private static void removeReportMenu(){
        System.out.println("\n=== Remove Report ===");
        int feedbackId = getIntInput("Enter Feedback ID to remove (Report): ");
        removeReport(feedbackId);
        System.out.println("If a report with that ID existed, it has been removed");
    }
    // Add a review to the database
    private static void addReview(FeedBack fb){
        String sql = "INSERT INTO feedback (buyer_Id, seller_id, listing_id, rating, rating_report, rating_desc) "
                + "VALUES (?, ?, ?, ?, FALSE, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fb.getBuyerId());
            stmt.setInt(2, fb.getSellerId());
            stmt.setInt(3, fb.getListingId());
            stmt.setInt(4, fb.getRating());
            stmt.setString(5, fb.getRatingDesc());

            stmt.executeUpdate();
            System.out.println("Feedback report saved to database.");
        } catch (SQLException e){
            System.out.println("Error saving feedback");
            e.printStackTrace();
        }
    }
    // Add a report to the database
    private static void addReport(FeedBack fb){
        String sql = "INSERT INTO feedback (buyer_Id, seller_id, listing_id, rating, rating_report, rating_desc, report_desc) "
                         + "VALUES (?, ?, ?, ?, TRUE, ?, ?)"; 
        try (Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, fb.getBuyerId());
                stmt.setInt(2, fb.getSellerId());
                stmt.setInt(3, fb.getListingId());
                stmt.setInt(4, fb.getRating());
                stmt.setString(5, fb.getRatingDesc());
                stmt.setString(6, fb.getReportDesc());

                stmt.executeUpdate();
                System.out.println("Feedback report saved to database.");
        } catch (SQLException e){
            System.out.println("Error saving feedback");
            e.printStackTrace();
        }
    }
    // Get the feedback from the database
    private static List<FeedBack> getFeedbackForListing(int listingId) {
        List<FeedBack> list = new ArrayList<>();

        String sql = "SELECT feedback_id, buyer_id, seller_id, listing_id, rating, rating_report, " +
                "rating_desc, report_desc, created_at " +
                "FROM feedback WHERE listing_id = ? ORDER BY created_at ASC";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isReport = rs.getBoolean("rating_report");
                int feedbackId   = rs.getInt("feedback_id");
                int buyerId      = rs.getInt("buyer_id");
                int sellerId     = rs.getInt("seller_id");
                int rating       = rs.getInt("rating");
                String ratingDesc = rs.getString("rating_desc");
                String reportDesc = rs.getString("report_desc");

                FeedBack fb;
                if (isReport) {
                    fb = new FeedBack(buyerId, sellerId, listingId, rating, ratingDesc, reportDesc);
                } else {
                    fb = new FeedBack(buyerId, sellerId, listingId, rating, ratingDesc);
                }
                fb.setFeedbackId(feedbackId);
                fb.setReport(isReport);

                list.add(fb);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    // Remove a review from the database
    private static void removeReview(int feedbackId) {
        String sql = "DELETE FROM feedback WHERE feedback_id = ? AND rating_report = FALSE";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, feedbackId);
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    // Remove a report from the database
    public static void removeReport(int feedbackId) {
        String sql = "DELETE FROM feedback WHERE feedback_id = ? AND rating_report = TRUE";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, feedbackId);
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    // Get the average from rating in the database
    private static double getAverageRatingAll(int listingId){
        String sql = "SELECT AVG(rating) FROM feedback WHERE listing_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listingId);
            ResultSet rs  = stmt.executeQuery();

            if (rs.next()){
                return rs.getDouble(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }
    // Get the average from reports in the database
    private static double getReviewAverage(int listingId){
        String sql = "SELECT AVG(rating) FROM feedback WHERE listing_id = ? AND rating_report = FALSE";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                return rs.getDouble(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }
    // Get review count from the database
    private static int getReviewCountDB(int listingId){
        String sql = "SELECT COUNT(*) FROM feedback WHERE listing_id = ? AND rating_report = FALSE";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }
    // Get the reports from the database
    public static int getReportCount(int listingId){
        String sql = "SELECT COUNT(*) FROM feedback WHERE listing_id = ?  AND rating_report = TRUE";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }
}
