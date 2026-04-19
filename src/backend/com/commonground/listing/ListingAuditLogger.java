package com.commonground.listing;
 
 
//Kelly needs to plug into for the Audit Trail feature.
// It writes a record to the audit_trail table after every listing operation.
//
// AuditTrail writes must be ASYNC so they don't
// slow down the user-facing response. That's why we use a background thread here.
//
 
//
// Kelly: if you need to expand the audit_trail table fields or change
//        how records are written, this is the only file you need to edit.
//        ListingService calls logAsync() and doesn't care how you store it.
 
 
import java.sql.*;
import java.time.Instant;
 
public class ListingAuditLogger {
 
   
    // Kelly swap these out for whatever your DB credentials are
    public static final String ACTION_CREATE = "CreateListing";
    public static final String ACTION_EDIT   = "EditListing";
    public static final String ACTION_DELETE = "DeleteListing";
    public static final String ACTION_STATUS = "StatusChange";
 
  
    // This is what ListingService calls after every successful operation.
    // It fires off a background thread so the audit write never blocks the UI.
    //
    // Parameters below
    //   listingId  =the listing this action was performed on
    //   clientId   =the seller/client who did it 
    //   action     =one of the ACTION_ constants above
    //   newStatus  = the status the listing was set to 
   
    public static void logAsync(int listingId, int clientId, String action, String newStatus) {
        // Capture timestamp immediately (before thread starts)
        String timestamp = Instant.now().toString();
 
        // Fire and forget — does not block the calling thread
        Thread auditThread = new Thread(() -> {
            try {
                writeRecord(listingId, clientId, action, newStatus, timestamp);
            } catch (Exception e) {
                // Audit failure should never crash the main operation
                System.err.println("[ListingAuditLogger] Failed to write audit record: " + e.getMessage());
            }
        });
 
        auditThread.setDaemon(true); // don't keep app alive just for audit writes
        auditThread.start();
    }
 
 
 
    // Inserts one row into the audit_trail table.
    // Kelly if your audit_trail table has different columns, update the SQL here.
   
    private static void writeRecord(int listingId, int clientId,
                                    String action, String newStatus,
                                    String timestamp) throws SQLException {
 
        String sql = "INSERT INTO audit_trail " +
                     "(listing_id, client_id, action, new_status) " +
                     "VALUES (?, ?, ?, ?)";
 
        try (Connection conn = com.commonground.util.DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt   (1, listingId);
            stmt.setInt   (2, clientId);
            stmt.setString(3, action);
            stmt.setString(4, newStatus != null ? newStatus : "N/A");
 
            stmt.executeUpdate();
            System.out.println("[ListingAuditLogger] Audit record written: "
                    + action + " | ListingID=" + listingId + " | ClientID=" + clientId);
        }
    }
}