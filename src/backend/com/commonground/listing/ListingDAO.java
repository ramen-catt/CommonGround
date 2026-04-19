
package com.commonground.listing;
//   to talk directly to MySQL. Everything else goes through here. Important for database team, this is a seperate storage 

//note to self add images to make sure they are entered through clients computer not to be stored in database 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListingDAO {

//random user I made to test, database people can edit here 
   //open new connection to test
    private Connection getConnection() throws SQLException {
        return com.commonground.util.DbUtil.getConnection();
    }


    /**
     * Inserts all categories from ListingCategory enum into the category table.
     * Dennis/Miguel P: you can also add these INSERT statements directly
     * to your SQL file if you prefer. Either way works.
     */
    public void seedCategories() throws SQLException {

        String sql = "INSERT IGNORE INTO category (category_name) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            
            for (String categoryName : ListingCategory.getAllDisplayNames()) {
                stmt.setString(1, categoryName);  
                stmt.executeUpdate();           
            }
        }
        
    }

    public void ensureImagePathCanStoreDataUrls() throws SQLException {
        String sql = "ALTER TABLE listing_image MODIFY file_path MEDIUMTEXT NOT NULL";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * UI team: call this to build your category <select> dropdown.
     * Example: for each item → option value=item[0], text=item[1]
     */
    public List<String[]> getAllCategories() throws SQLException {

        String sql = "SELECT category_id, category_name FROM category ORDER BY category_name";
        List<String[]> categories = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(new String[]{
                    String.valueOf(rs.getInt("category_id")),  // e.g. "5"
                    rs.getString("category_name")              // e.g. "Electronics"
                });
            }
        }
        return categories;
    }

    /**
     * GET CATEGORY ID BY NAME
     * The user picks "Electronics" from the dropdown. We need the ID number example 5 to save in the listing table.
     * Returns -1 if the name is not found in the DB.
     */
    public int getCategoryIdByName(String categoryName) throws SQLException {

        String sql = "SELECT category_id FROM category WHERE category_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("category_id"); // found it — return the ID
            }
        }
        return -1; 
    }


    //create listing
    public int insert(Listing listing) throws SQLException {

       // security!!!
        // Using placeholders (PreparedStatement) prevents SQL injection attacks
    
        String sql = "INSERT INTO listing " +
                     "(client_id, location_id, category_id, payment_type, " +
                     " item_name, price, description, item_condition, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // MySQL to give us back the auto-generated listing_id
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

        
            stmt.setInt   (1, listing.getClientId());     
            stmt.setInt   (2, listing.getLocationId());   
            stmt.setInt   (3, listing.getCategoryId());   
            stmt.setString(4, listing.getPaymentType());  
            stmt.setString(5, listing.getItemName());      
            stmt.setDouble(6, listing.getPrice());       
            stmt.setString(7, listing.getDescription());   
            stmt.setString(8, listing.getItemCondition());
            stmt.setString(9, Listing.STATUS_AVAILABLE);  

            stmt.executeUpdate(); // run the INSERT

          //read back
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                listing.setListingId(newId); // update the listing object
                return newId;
            }
        }
        return -1; // something went wrong if we get here
    }

   //loads the listing
    public Listing findById(int listingId) throws SQLException {

        // JOIN with category table so we get category_name in one query
        String sql = "SELECT l.*, c.category_name, a.username AS seller_name, a.email AS seller_email, a.created_at AS seller_created_at " +
                     "FROM listing l " +
                     "JOIN category c ON l.category_id = c.category_id " +
                     "JOIN account a ON l.client_id = a.account_id " +
                     "WHERE l.listing_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRowToListing(rs); // convert DB row → Listing object
            }
        }
        return null;
    }

  //checks all avalaible to see 
    public List<Listing> findAllAvailable() throws SQLException {

        String sql = "SELECT l.*, c.category_name, a.username AS seller_name, a.email AS seller_email, a.created_at AS seller_created_at " +
                     "FROM listing l " +
                     "JOIN category c ON l.category_id = c.category_id " +
                     "JOIN account a ON l.client_id = a.account_id " +
                     "WHERE l.status = ? " +
                     "ORDER BY l.created_at DESC"; // newest listings first

        List<Listing> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, Listing.STATUS_AVAILABLE);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRowToListing(rs));
            }
        }
        return results;
    }

   //finds by category 
    public List<Listing> findByCategory(String categoryName) throws SQLException {

        String sql = "SELECT l.*, c.category_name, a.username AS seller_name, a.email AS seller_email, a.created_at AS seller_created_at " +
                     "FROM listing l " +
                     "JOIN category c ON l.category_id = c.category_id " +
                     "JOIN account a ON l.client_id = a.account_id " +
                     "WHERE l.status = ? AND c.category_name = ? " +
                     "ORDER BY l.created_at DESC";

        List<Listing> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, Listing.STATUS_AVAILABLE);
            stmt.setString(2, categoryName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRowToListing(rs));
            }
        }
        return results;
    }

    public List<Listing> findByClientId(int clientId) throws SQLException {

        String sql = "SELECT l.*, c.category_name, a.username AS seller_name, a.email AS seller_email, a.created_at AS seller_created_at " +
                     "FROM listing l " +
                     "JOIN category c ON l.category_id = c.category_id " +
                     "JOIN account a ON l.client_id = a.account_id " +
                     "WHERE l.client_id = ? AND l.status != ? " +
                     "ORDER BY l.created_at DESC";

        List<Listing> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt   (1, clientId);
            stmt.setString(2, Listing.STATUS_REMOVED); // hide soft-deleted ones
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRowToListing(rs));
            }
        }
        return results;
    }

   //search I thinkwe decided to not use this if not I can delete 
    public List<Listing> search(String keyword) throws SQLException {

        String sql = "SELECT l.*, c.category_name, a.username AS seller_name, a.email AS seller_email, a.created_at AS seller_created_at " +
                     "FROM listing l " +
                     "JOIN category c ON l.category_id = c.category_id " +
                     "JOIN account a ON l.client_id = a.account_id " +
                     "WHERE l.status = ? " +
                     "AND (l.item_name LIKE ? OR l.description LIKE ?) " +
                     "ORDER BY l.created_at DESC";

        List<Listing> results = new ArrayList<>();
        String pattern = "%" + keyword + "%"; // wrap keyword so LIKE finds it anywhere

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, Listing.STATUS_AVAILABLE);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRowToListing(rs));
            }
        }
        return results;
    }


    public boolean update(Listing listing) throws SQLException {

        String sql = "UPDATE listing SET " +
                     "item_name=?, description=?, price=?, " +
                     "category_id=?, item_condition=?, payment_type=? " +
                     "WHERE listing_id=? AND client_id=? AND status=?";
       

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, listing.getItemName());
            stmt.setString(2, listing.getDescription());
            stmt.setDouble(3, listing.getPrice());
            stmt.setInt   (4, listing.getCategoryId());
            stmt.setString(5, listing.getItemCondition());
            stmt.setString(6, listing.getPaymentType());
            stmt.setInt   (7, listing.getListingId());
            stmt.setInt   (8, listing.getClientId());
            stmt.setString(9, Listing.STATUS_AVAILABLE); 

           
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * UPDATE STATUS Change only the status of a listing.
     *
     *   • Transaction team (Kelly):  Reserved (buyer initiates), Sold (both confirm),
     *                                Available again (if cancelled)
     */
    public boolean updateStatus(int listingId, int clientId, String newStatus)
            throws SQLException {

        String sql = "UPDATE listing SET status=? WHERE listing_id=? AND client_id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt   (2, listingId);
            stmt.setInt   (3, clientId);

            return stmt.executeUpdate() > 0;
        }
    }


 
    // soft delete they just change the status to removed instead of deleting the row, we have other things such as trasnactions and feedback who use it, allows sellers to delete something that is not sold yet
    public boolean softDelete(int listingId, int clientId) throws SQLException {

        // AND status != 'Sold' → physically blocks deleting a Sold listing at DB level
        String sql = "UPDATE listing SET status=? " +
                     "WHERE listing_id=? AND client_id=? AND status != ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, Listing.STATUS_REMOVED); 
            stmt.setInt   (2, listingId);
            stmt.setInt   (3, clientId);
            stmt.setString(4, Listing.STATUS_SOLD);      // block if already Sold 

            return stmt.executeUpdate() > 0;
        }
    }



    // IMAGE 
    //   So the process is:
    //     1. User picks an image file on their computer (file upload in UI)
    //     2. The servlet saves the file to a folder on the server
    //        (/uploads/listings/listing_5_photo1.jpg)
    //     3. The servlet calls service.addImage(listingId, fileName, filePath)
    //     4. ListingService calls addImage() here in the DAO
    //     5. We only save the file_name and file_path strings in MySQL
 


    public void addImage(int listingId, String fileName, String filePath)
            throws SQLException {

        String sql = "INSERT INTO listing_image (listing_id, file_name, file_path) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt   (1, listingId);
            stmt.setString(2, fileName);
            stmt.setString(3, filePath);
            stmt.executeUpdate();
        }
    }

    public void replaceImages(int listingId, String fileName, String filePath)
            throws SQLException {

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM listing_image WHERE listing_id = ?");
                 PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO listing_image (listing_id, file_name, file_path) VALUES (?, ?, ?)")) {

                delete.setInt(1, listingId);
                delete.executeUpdate();

                insert.setInt(1, listingId);
                insert.setString(2, fileName);
                insert.setString(3, filePath);
                insert.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<String[]> getImages(int listingId) throws SQLException {

        String sql = "SELECT file_name, file_path FROM listing_image " +
                     "WHERE listing_id = ? ORDER BY created_at ASC";

        List<String[]> images = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                images.add(new String[]{
                    rs.getString("file_name"),
                    rs.getString("file_path")
                });
            }
        }
        return images;
    }

    // PRIVATE HELPER Convert one database row into a Listing object
  
    private Listing mapRowToListing(ResultSet rs) throws SQLException {
        Listing l = new Listing();

        l.setListingId   (rs.getInt   ("listing_id"));
        l.setClientId    (rs.getInt   ("client_id"));
        l.setLocationId  (rs.getInt   ("location_id"));
        l.setCategoryId  (rs.getInt   ("category_id"));
        l.setCategoryName(rs.getString("category_name")); 
        l.setItemName    (rs.getString("item_name"));
        l.setPrice       (rs.getDouble("price"));
        l.setDescription (rs.getString("description"));
        l.setItemCondition(rs.getString("item_condition"));
        l.setPaymentType (rs.getString("payment_type"));
        l.setStatus      (rs.getString("status"));
        l.setCreatedAt     (rs.getString("created_at"));
        l.setSellerName    (rs.getString("seller_name"));
        l.setSellerEmail   (rs.getString("seller_email"));
        java.sql.Timestamp sellerTs = rs.getTimestamp("seller_created_at");
        l.setSellerJoinDate(sellerTs != null
                ? new java.text.SimpleDateFormat("MMMM yyyy").format(sellerTs)
                : "");

        return l;
    }
}
