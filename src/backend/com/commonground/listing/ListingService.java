package com.commonground.listing;
//   This file is the "brain" of the listing feature. It:
//     1. Takes input from the UI (or test)
//     2. VALIDATES everything (required fields, dropdown values, status rules)
//     3. If valid → calls ListingDAO to do the database work
//     4. Returns a ServiceResult so the caller knows what happened
//UI area 
import java.sql.SQLException;
import java.util.List;

public class ListingService {

    //connection to the SQL server 
    private final ListingDAO listingDAO;


    public ListingService() {
        this.listingDAO = new ListingDAO();
    }



    // Seeds all categories into the category table.
    // Safe to call every startup (uses INSERT IGNORE in DAO).

    public void initialize() {
        try {
            listingDAO.ensureImagePathCanStoreDataUrls();
            listingDAO.seedCategories();
            System.out.println("[ListingService] Categories seeded successfully.");
        } catch (SQLException e) {
            System.err.println("[ListingService] Could not seed categories: " + e.getMessage());
        }
    }


 //dropdown UI part 
    public List<String[]> getAllCategories() {
        try {
            return listingDAO.getAllCategories();
        } catch (SQLException e) {
            System.err.println("[ListingService] Error loading categories: " + e.getMessage());
            return List.of(); 
        }
    }

   //all allowed categories
    public String[] getAllowedConditions() {
        return Listing.ALLOWED_CONDITIONS;
    }

  //dropdown all allowed payment (probably wont use this)
    public String[] getAllowedPaymentTypes() {
        return Listing.ALLOWED_PAYMENT_TYPES;
    }


    public ServiceResult createListing(int clientId, int locationId,
                                       String categoryName, String itemName,
                                       double price, String description,
                                       String itemCondition, String paymentType) {

  

        // Must be logged in to create a listing
        if (clientId <= 0) {
            return ServiceResult.fail("You must be logged in to create a listing.");
        }

        // location_id required by the database 
        if (locationId <= 0) {
            return ServiceResult.fail("A valid location is required.");
        }

    
        if (itemName == null || itemName.isBlank()) {
            return ServiceResult.fail("Item name is required. Please enter a title for your listing.");
        }
    
        if (itemName.trim().length() > 100) {
            return ServiceResult.fail("Item name is too long. Please keep it under 100 characters.");
        }

      
        if (description == null || description.isBlank()) {
            return ServiceResult.fail("Description is required. Tell buyers about your item.");
        }

      
        if (price <= 0.0) {
            return ServiceResult.fail("Price must be greater than $0.00.");
        }

    
        if (!ListingCategory.isValid(categoryName)) {
            return ServiceResult.fail("Please select a valid category from the dropdown list.");
        }

    
        if (!Listing.isValidCondition(itemCondition)) {
            return ServiceResult.fail("Please select a valid item condition.");
        }

    
        if (!Listing.isValidPaymentType(paymentType)) {
            return ServiceResult.fail("Please select a valid payment type.");
        }

     //look up category with number ID
        int categoryId;
        try {
            categoryId = listingDAO.getCategoryIdByName(categoryName);
            if (categoryId == -1) {
                listingDAO.seedCategories();
                categoryId = listingDAO.getCategoryIdByName(categoryName);
            }
        } catch (SQLException e) {
            System.err.println("[ListingService] Error looking up category: " + e.getMessage());
            return ServiceResult.fail("Error looking up category. Please try again.");
        }

        // If category not found in DB, seeding probably hasn't run yet
        if (categoryId == -1) {
            return ServiceResult.fail(
                "Category not found in database. Please run initialize() first."
            );
        }

      //passed valiation
        Listing listing = new Listing(
            clientId,
            locationId,
            categoryId,
            itemName.trim(),       // trim removes extra spaces from start/end
            price,
            description.trim(),
            itemCondition,
            paymentType
        );
    try {
            int newId = listingDAO.insert(listing);
            if (newId > 0) {
                // AUDIT TRAIL - KELLY
                // Runs async so it doesn't slow down the response (we said this in the document)
                ListingAuditLogger.logAsync(
                    newId, clientId,
                    ListingAuditLogger.ACTION_CREATE,
                    Listing.STATUS_AVAILABLE
                );
               
 
                return ServiceResult.success(
                    "Listing created! Your item is now available for buyers to see.",
                    newId
                );
            } else {
                return ServiceResult.fail("Failed to save listing. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in createListing: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }

    /**
     * Get one listing by its ID.
     * Returns null if not found.
     */
    public Listing getListingById(int listingId) {
        try {
            return listingDAO.findById(listingId);
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in getListingById: " + e.getMessage());
            return null;
        }
    }


    public List<Listing> getAllAvailableListings() {
        try {
            return listingDAO.findAllAvailable();
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in getAllAvailableListings: " + e.getMessage());
            return List.of();
        }
    }


    public List<Listing> getListingsByCategory(String categoryName) {
        // Validate category before hitting the DB
        if (!ListingCategory.isValid(categoryName)) {
            return List.of(); // invalid category = no results
        }
        try {
            return listingDAO.findByCategory(categoryName);
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in getListingsByCategory: " + e.getMessage());
            return List.of();
        }
    }

   //shows accounts listings
    public List<Listing> getMyListings(int clientId) {
        try {
            return listingDAO.findByClientId(clientId);
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in getMyListings: " + e.getMessage());
            return List.of();
        }
    }

  //search (Might not do this if so I added it)
    public List<Listing> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllAvailableListings(); // blank search = show everything
        }
        try {
            return listingDAO.search(keyword.trim());
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in search: " + e.getMessage());
            return List.of();
        }
    }


//edit listing

    
    public ServiceResult editListing(int listingId, int clientId,
                                     String categoryName, String itemName,
                                     double price, String description,
                                     String itemCondition, String paymentType) {

        
        Listing existing;
        try {
            existing = listingDAO.findById(listingId);
        } catch (SQLException e) {
            return ServiceResult.fail("Could not load listing. Please try again.");
        }

        if (existing == null) {
            return ServiceResult.fail("Listing not found.");
        }

        //owner ship check
        if (existing.getClientId() != clientId) {
            return ServiceResult.fail("You can only edit your own listings.");
        }

      //cannot edit if sold
        if (!existing.isEditable()) {
            return ServiceResult.fail(
                "This listing cannot be edited because it is currently '" +
                existing.getStatus() + "'. Only Available listings can be edited."
            );
        }

        //  Validate new values 
        if (itemName == null || itemName.isBlank()) {
            return ServiceResult.fail("Item name is required.");
        }
        if (itemName.trim().length() > 100) {
            return ServiceResult.fail("Item name must be 100 characters or less.");
        }
        if (description == null || description.isBlank()) {
            return ServiceResult.fail("Description is required.");
        }
        if (price <= 0.0) {
            return ServiceResult.fail("Price must be greater than $0.00.");
        }
        if (!ListingCategory.isValid(categoryName)) {
            return ServiceResult.fail("Please select a valid category from the dropdown list.");
        }
        if (!Listing.isValidCondition(itemCondition)) {
            return ServiceResult.fail("Please select a valid item condition.");
        }
        if (!Listing.isValidPaymentType(paymentType)) {
            return ServiceResult.fail("Please select a valid payment type.");
        }

        // Look up category ID
        int categoryId;
        try {
            categoryId = listingDAO.getCategoryIdByName(categoryName);
            if (categoryId == -1) {
                listingDAO.seedCategories();
                categoryId = listingDAO.getCategoryIdByName(categoryName);
            }
        } catch (SQLException e) {
            return ServiceResult.fail("Error looking up category. Please try again.");
        }
        if (categoryId == -1) {
            return ServiceResult.fail("Category not found in database.");
        }

        // Apply changes
        existing.setItemName(itemName.trim());
        existing.setDescription(description.trim());
        existing.setPrice(price);
        existing.setCategoryId(categoryId);
        existing.setItemCondition(itemCondition);
        existing.setPaymentType(paymentType);

        // Save to database 
        try {
            boolean updated = listingDAO.update(existing);
            if (updated) {
                // AUDIT TRAIL - KELLY
                ListingAuditLogger.logAsync(listingId, clientId,
                        ListingAuditLogger.ACTION_EDIT, existing.getStatus());

                return ServiceResult.success("Listing updated successfully.", listingId);
            } else {
                return ServiceResult.fail(
                    "Update failed. The listing may have changed status. Please refresh and try again."
                );
            }
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in editListing: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }


    //delete soft
    public ServiceResult deleteListing(int listingId, int clientId) {

    
        Listing existing;
        try {
            existing = listingDAO.findById(listingId);
        } catch (SQLException e) {
            return ServiceResult.fail("Could not load listing. Please try again.");
        }

        if (existing == null) {
            return ServiceResult.fail("Listing not found.");
        }

        // Ownership check
        if (existing.getClientId() != clientId) {
            return ServiceResult.fail("You can only delete your own listings.");
        }

        //isDeletable() returns false if status is "Sold"
        if (!existing.isDeletable()) {
            return ServiceResult.fail(
                "This listing cannot be deleted because it has already been sold."
            );
        }

        try {
            boolean deleted = listingDAO.softDelete(listingId, clientId);
            if (deleted) {
                // AUDIT TRAIL - KELLY
                ListingAuditLogger.logAsync(listingId, clientId,
                        ListingAuditLogger.ACTION_DELETE, Listing.STATUS_REMOVED);

                return ServiceResult.success(
                    "Your listing has been removed successfully.", listingId
                );
            } else {
                return ServiceResult.fail("Could not delete listing. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in deleteListing: " + e.getMessage());
            return ServiceResult.fail("A database error occurred. Please try again.");
        }
    }



    //displays status to both buyer and seller, 
    // Kelly would look here for:
    // Listing.STATUS_RESERVED  → buyer initiated purchase
    //   Listing.STATUS_SOLD      → both parties confirmed, deal done
    //   Listing.STATUS_AVAILABLE → transaction cancelled, put back available

    public ServiceResult updateStatus(int listingId, int clientId, String newStatus) {
        // Validate that the new status is one of the allowed values
        if (!newStatus.equals(Listing.STATUS_AVAILABLE) &&
            !newStatus.equals(Listing.STATUS_RESERVED)  &&
            !newStatus.equals(Listing.STATUS_SOLD)       &&
            !newStatus.equals(Listing.STATUS_REMOVED)) {
            return ServiceResult.fail("Invalid status value: " + newStatus);
        }

        try {
            boolean updated = listingDAO.updateStatus(listingId, clientId, newStatus);
            if (updated) {
                // AUDIT TRAIL - KELLY
                ListingAuditLogger.logAsync(listingId, clientId,
                        ListingAuditLogger.ACTION_STATUS, newStatus);

                return ServiceResult.success(
                    "Listing status updated to: " + newStatus, listingId
                );
            } else {
                return ServiceResult.fail(
                    "Status update failed. Listing may not exist or may not be owned by this client."
                );
            }
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in updateStatus: " + e.getMessage());
            return ServiceResult.fail("A database error occurred.");
        }
    }


    //image area 
    public ServiceResult addImage(int listingId, String fileName, String filePath) {
        if (fileName == null || fileName.isBlank()) {
            return ServiceResult.fail("Image file name cannot be empty.");
        }
        if (filePath == null || filePath.isBlank()) {
            return ServiceResult.fail("Image file path cannot be empty.");
        }
        try {
            listingDAO.addImage(listingId, fileName.trim(), filePath.trim());
            return ServiceResult.success("Image saved successfully.", listingId);
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in addImage: " + e.getMessage());
            return ServiceResult.fail("Could not save image. Please try again.");
        }
    }

    public ServiceResult replaceImages(int listingId, String fileName, String filePath) {
        if (fileName == null || fileName.isBlank()) {
            return ServiceResult.fail("Image file name cannot be empty.");
        }
        if (filePath == null || filePath.isBlank()) {
            return ServiceResult.fail("Image file path cannot be empty.");
        }
        try {
            listingDAO.replaceImages(listingId, fileName.trim(), filePath.trim());
            return ServiceResult.success("Image updated successfully.", listingId);
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in replaceImages: " + e.getMessage());
            return ServiceResult.fail("Could not update image. Please try again.");
        }
    }

    /**
     * Returns all image records for a listing.
     * Each entry is String[]: [0]=file_name, [1]=file_path
     */
    public List<String[]> getImages(int listingId) {
        try {
            return listingDAO.getImages(listingId);
        } catch (SQLException e) {
            System.err.println("[ListingService] DB error in getImages: " + e.getMessage());
            return List.of();
        }
    }
}
