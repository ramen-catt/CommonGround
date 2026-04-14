package com.commonground.admin;


//   1. Takes input from UI
//   2. Checks the caller is actually an admin before doing anything
//   3. Calls AdminDAO to do the database work
//   4. Returns a ServiceResult so the UI knows what happened
//
// Admins can remove fraudulent listings, suspend users,
// view reports, and perform updates.
// Admins are NOT permitted to buy or sell using admin profile.

import com.commonground.listing.ServiceResult;
import java.sql.SQLException;
import java.util.List;

public class AdminService {

    // connection to the DAO
    private final AdminDAO adminDAO;

    public AdminService() {
        this.adminDAO = new AdminDAO();
    }


  
    // If the caller is not an admin, nothing runs.

    private boolean verifyAdmin(int adminAccountId) {
        try {
            return adminDAO.isAdmin(adminAccountId);
        } catch (SQLException e) {
            System.err.println("[AdminService] Error verifying admin: " + e.getMessage());
            return false;
        }
    }


    //suspend someone 
    // UI team: call this from the admin dashboard suspend button.

    public ServiceResult suspendUser(int adminAccountId, int targetAccountId) {

        if (!verifyAdmin(adminAccountId)) {
            return ServiceResult.fail("Access denied. Admin privileges required.");
        }

        // cannot suspend yourself
        if (adminAccountId == targetAccountId) {
            return ServiceResult.fail("You cannot suspend your own admin account.");
        }

        try {
            boolean done = adminDAO.setSuspended(targetAccountId, true);
            if (done) {
                return ServiceResult.success(
                    "User account " + targetAccountId + " has been suspended.", targetAccountId);
            } else {
                return ServiceResult.fail(
                    "Could not suspend user. They may not exist or may be another admin.");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in suspendUser: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }


//undo the suspend 

    public ServiceResult unsuspendUser(int adminAccountId, int targetAccountId) {

        if (!verifyAdmin(adminAccountId)) {
            return ServiceResult.fail("Access denied. Admin privileges required.");
        }

        try {
            boolean done = adminDAO.setSuspended(targetAccountId, false);
            if (done) {
                return ServiceResult.success(
                    "User account " + targetAccountId + " has been reinstated.", targetAccountId);
            } else {
                return ServiceResult.fail("Could not reinstate user. They may not exist.");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in unsuspendUser: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }


//remove fraudulent listing 
    // Unlike seller soft delete, admin can remove ANY listing including sold ones.

    public ServiceResult removeListing(int adminAccountId, int listingId) {

        if (!verifyAdmin(adminAccountId)) {
            return ServiceResult.fail("Access denied. Admin privileges required.");
        }

        if (listingId <= 0) {
            return ServiceResult.fail("Invalid listing ID.");
        }

        try {
            boolean done = adminDAO.adminRemoveListing(listingId);
            if (done) {
                return ServiceResult.success(
                    "Listing " + listingId + " has been removed.", listingId);
            } else {
                return ServiceResult.fail(
                    "Could not remove listing. It may not exist.");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in removeListing: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }



    // viewing page for fraud reports 
    // admin needs to look and view them first to accept or deny the request
    // Returns list of reports each entry is:
    // [0]=feedback_id [1]=buyer_id [2]=seller_id [3]=listing_id
    // [4]=rating_desc [5]=review_desc [6]=created_at

    public List<String[]> viewReports(int adminAccountId) {

        if (!verifyAdmin(adminAccountId)) {
            System.err.println("[AdminService] Unauthorized report view attempt.");
            return List.of();
        }

        try {
            return adminDAO.getAllReports();
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in viewReports: " + e.getMessage());
            return List.of();
        }
    }


  //deleting a review

    public ServiceResult removeReview(int adminAccountId, int feedbackId) {

        if (!verifyAdmin(adminAccountId)) {
            return ServiceResult.fail("Access denied. Admin privileges required.");
        }

        if (feedbackId <= 0) {
            return ServiceResult.fail("Invalid feedback ID.");
        }

        try {
            boolean done = adminDAO.removeReview(feedbackId);
            if (done) {
                return ServiceResult.success(
                    "Review " + feedbackId + " has been removed.", feedbackId);
            } else {
                return ServiceResult.fail(
                    "Could not remove review. It may not exist.");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in removeReview: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }


 //look at all accounts 
    // [0]=account_id [1]=username [2]=email [3]=is_admin [4]=is_suspended

    public List<String[]> viewAllAccounts(int adminAccountId) {

        if (!verifyAdmin(adminAccountId)) {
            System.err.println("[AdminService] Unauthorized account view attempt.");
            return List.of();
        }

        try {
            return adminDAO.getAllAccounts();
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in viewAllAccounts: " + e.getMessage());
            return List.of();
        }
    }


  //transaction logn
    // [0]=transaction_id [1]=buyer_id [2]=seller_id [3]=listing_id
    // [4]=status [5]=transaction_time

    public List<String[]> viewTransactionLog(int adminAccountId) {

        if (!verifyAdmin(adminAccountId)) {
            System.err.println("[AdminService] Unauthorized transaction log view attempt.");
            return List.of();
        }

        try {
            return adminDAO.getAllTransactions();
        } catch (SQLException e) {
            System.err.println("[AdminService] DB error in viewTransactionLog: " + e.getMessage());
            return List.of();
        }
    }
}