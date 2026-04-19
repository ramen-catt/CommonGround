
package com.commonground.listing;
//categories 

public enum ListingCategory {

//clothing & Accessories
    CLOTHING_MENS    ("Men's Clothing"),
    CLOTHING_WOMENS  ("Women's Clothing"),
    CLOTHING_KIDS    ("Kids' Clothing"),
    SHOES            ("Shoes & Footwear"),
    BAGS_ACCESSORIES ("Bags & Accessories"),
    JEWELRY          ("Jewelry & Watches"),

//Electronics 
   
    ELECTRONICS      ("Electronics"),
    PHONES_TABLETS   ("Phones & Tablets"),
    COMPUTERS        ("Computers & Laptops"),
    GAMING           ("Gaming"),
    CAMERAS          ("Cameras & Photography"),

 //Home living
    FURNITURE        ("Furniture"),
    APPLIANCES       ("Appliances"),
    KITCHEN          ("Kitchen & Dining"),
    TOOLS            ("Tools & Hardware"),

//Media & Learning

    BOOKS            ("Books & Magazines"),
    MUSIC            ("Music & Instruments"),
    MOVIES_TV        ("Movies & TV"),

// Hobbies & entertainment games
 
    SPORTS_OUTDOORS  ("Sports & Outdoors"),
    TOYS_GAMES       ("Toys & Games"),
    ART_COLLECTIBLES ("Art & Collectibles"),

//other
    BABY_KIDS        ("Baby & Kids"),
    BEAUTY           ("Beauty & Personal Care"),
    HEALTH           ("Health & Wellness"),
    AUTOMOTIVE       ("Automotive"),
    PET_SUPPLIES     ("Pet Supplies"),
    

    // these match the frontend dropdown values exactly
    
    HOME_DECOR       ("Home Decor"),
    OFFICE_EQUIPMENT ("Office Equipment"),
    OTHER            ("Other");

    //shown in database for reable version
    private final String displayName;

    // Constructor Java calls this automatically for each enum value above
    ListingCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable label for this category.
     * Example: ListingCategory.PHONES_TABLETS.getDisplayName()    → "Phones & Tablets"
     * This is what gets stored in the database and shown in the UI.
     */
    public String getDisplayName() {
        return displayName;
    }


  //checks for valid category 

    public static boolean isValid(String displayName) {
        if (displayName == null || displayName.isBlank()) return false;
        // Loop through every enum value and check if the label matches
        for (ListingCategory cat : values()) {
            if (cat.displayName.equals(displayName)) return true;
        }
        return false;
    }

    // UI people helper makes a simple String array to build a dropdown 
    public static String[] getAllDisplayNames() {
        ListingCategory[] all = values(); // get all enum values
        String[] names = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            names[i] = all[i].displayName;
        }
        return names;
    }

    /**
     * makes printing/debugging easier.
     * Instead of printing the code name (PHONES_TABLETS),
     * it prints the display name ("Phones & Tablets").
     */
    @Override
    public String toString() {
        return displayName;
    }
}
