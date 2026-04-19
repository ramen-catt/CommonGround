package com.commonground.listing;

// This is basically the blueprint for what a "listing" is in our app.
// Every time someone posts an item for sale, one of these Listing objects gets created.
// Think of it like a form it holds all the info: item name, price, category, condition, etc.
// This file does NOT save anything to the database, that's ListingDAO's job.
// This file does NOT check if stuff is valid, that's ListingService's job.
// This is just the container that holds all the listing data.
public class Listing {
//used for race conditions, also used for the transaction Kelly 
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_RESERVED  = "Reserved";
    public static final String STATUS_SOLD      = "Sold";
    public static final String STATUS_REMOVED   = "Removed";

    public static final String[] ALLOWED_CONDITIONS = {
        "New",       
        "Like New",  
        "Good",      
        "Fair",      
        "Poor"       
    };

   //how someone would like to pay not sure if there is a UI function for this if not I just added it
    public static final String[] ALLOWED_PAYMENT_TYPES = {"App Payment",  "Cash"  };


//fields in database

//listing_id — the unique ID for this listing.

    private int listingId;

//who created it client from account id
    private int clientId;

    private int locationId;

    private int categoryId;


    //This is for UI since it doesnt take the number like above for the database input
    private String categoryName;

    //name of item such as Iphone, shirt 
    private String itemName;

    private double price;

    private String description;

    //used, like new, etc
    private String itemCondition;

    private String paymentType;

    private String status;

  //created datetime
    private String createdAt;

    private String sellerName;

    private String sellerEmail;

    private String sellerJoinDate;


  //constructors 

    //default fillers for constructors so it doesnt break
    public Listing() {
        this.status      = STATUS_AVAILABLE; 
        this.paymentType = "App Payment";    
    }

    public Listing(int clientId, int locationId, int categoryId,
                   String itemName, double price, String description,
                   String itemCondition, String paymentType) {
        this.clientId      = clientId;
        this.locationId    = locationId;
        this.categoryId    = categoryId;
        this.itemName      = itemName;
        this.price         = price;
        this.description   = description;
        this.itemCondition = itemCondition;
        this.paymentType   = paymentType;
        this.status        = STATUS_AVAILABLE; 
    }


   //getters
    public int    getListingId()     { return listingId; }
    public int    getClientId()      { return clientId; }
    public int    getLocationId()    { return locationId; }
    public int    getCategoryId()    { return categoryId; }
    public String getCategoryName()  { return categoryName; }
    public String getItemName()      { return itemName; }
    public double getPrice()         { return price; }
    public String getDescription()   { return description; }
    public String getItemCondition() { return itemCondition; }
    public String getPaymentType()   { return paymentType; }
    public String getStatus()        { return status; }
    public String getCreatedAt()     { return createdAt; }
    public String getSellerName()     { return sellerName; }
    public String getSellerEmail()    { return sellerEmail; }
    public String getSellerJoinDate() { return sellerJoinDate; }

   //adds $ to it for the UI team 
    public String getPriceDisplay() {
        return String.format("$%.2f", price);
    }


 //others can update each setters
    public void setListingId(int id)             { this.listingId = id; }
    public void setClientId(int id)              { this.clientId = id; }
    public void setLocationId(int id)            { this.locationId = id; }
    public void setCategoryId(int id)            { this.categoryId = id; }
    public void setCategoryName(String name)     { this.categoryName = name; }
    public void setItemName(String itemName)     { this.itemName = itemName; }
    public void setPrice(double price)           { this.price = price; }
    public void setDescription(String desc)      { this.description = desc; }
    public void setItemCondition(String cond)    { this.itemCondition = cond; }
    public void setPaymentType(String type)      { this.paymentType = type; }
    public void setStatus(String status)         { this.status = status; }
    public void setCreatedAt(String createdAt)   { this.createdAt = createdAt; }
    public void setSellerName(String sellerName)       { this.sellerName = sellerName; }
    public void setSellerEmail(String sellerEmail)     { this.sellerEmail = sellerEmail; }
    public void setSellerJoinDate(String sellerJoinDate) { this.sellerJoinDate = sellerJoinDate; }


  //rules 

   //only avaliable status can be purchased 
    public boolean isAvailable() {
        return STATUS_AVAILABLE.equals(this.status);
    }


    // avalable lisitngs are editable unlike others "sold"
    public boolean isEditable() {
        return STATUS_AVAILABLE.equals(this.status);
    }


    //allows sellers can delete listings not yet sold
    public boolean isDeletable() {
        return !STATUS_SOLD.equals(this.status);
    }

 //checks if what person searches is on the list 

    public static boolean isValidCondition(String condition) {
        if (condition == null || condition.isBlank()) return false;
        for (String c : ALLOWED_CONDITIONS) {
            if (c.equals(condition)) return true;
        }
        return false;
    }

   //validates payment choice 
    public static boolean isValidPaymentType(String paymentType) {
        if (paymentType == null || paymentType.isBlank()) return false;
        for (String t : ALLOWED_PAYMENT_TYPES) {
            if (t.equals(paymentType)) return true;
        }
        return false;
    }


//testing
    @Override
    public String toString() {
        return "Listing{" +
               "id=" + listingId +
               ", seller=" + clientId +
               ", item='" + itemName + '\'' +
               ", price=" + getPriceDisplay() +
               ", category='" + categoryName + "' (id=" + categoryId + ")" +
               ", condition='" + itemCondition + '\'' +
               ", status='" + status + '\'' +
               ", created=" + createdAt +
               '}';
    }
}
