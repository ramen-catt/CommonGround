package com.commonground.listing;

//   A small helper class. Every method in ListingService returns
//   one of these objects instead of returning raw values.
// Helps UI team without this the UI team would have to catch exceptions,
//   check for -1 return values, etc. messy and confusing.
// every method in ListingService returns one of these 

// confirms if its a success 

public class ServiceResult {

    
    /**
     * Did the operation succeed?
     * true = everything worked
     * false = something failed (see message for why)
     */
    private final boolean success;

    /**
     * A human-readable message about what happened.
     * On success: "Listing created! Your item is now available."
     * On failure: "Item name is required." or "A database error occurred."
     */
    private final String message;

    /**
     * The listing_id of the record involved.
     * Only meaningful when success = true and an ID exists.
     * Set to -1 when not applicable
     */
    private final int id;



    // constructor private so no one can call it directly.
    // Use the factory methods below instead (success() and fail()).
    
    private ServiceResult(boolean success, String message, int id) {
        this.success = success;
        this.message = message;
        this.id      = id;
    }


    
   
    

    //Call this when the operation worked, the only way to create a ServiceResult
 
    public static ServiceResult success(String message, int id) {
        return new ServiceResult(true, message, id);
    }

    
    public static ServiceResult fail(String message) {
        return new ServiceResult(false, message, -1);
    }


 //getter 
    public boolean isSuccess() { return success; }


    public String getMessage() { return message; }

   
    public int getId() { return id; }


    //printing the outputs 
    @Override
    public String toString() {
        return (success ? "SUCCESS" : "FAIL") +
               ": " + message +
               (id > 0 ? " (ID=" + id + ")" : "");
    }
}
