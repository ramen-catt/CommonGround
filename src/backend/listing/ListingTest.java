package com.commonground.listing;

//Ran this in the terminal VS code 
//   1: Run CommonGround_db.sql in MySQL Workbench
//           (Dennis/Miguel P's main database file 
//
//   2: Run listing_setup.sql in MySQL Workbench
//           (creates the test account, client, and location you need)
//   3. Had to create a new user because it wasnt connecting to my local host ran this command in a new query
//CREATE USER 'gguser'@'localhost' IDENTIFIED BY 'ggpass123';
//GRANT ALL PRIVILEGES ON commonground_db.* TO 'gguser'@'localhost';
//FLUSH PRIVILEGES;-- this is what you see in the DOA user name and pass
//
//   4: Update DB_PASS in ListingDAO.java to your MySQL password I changed it to mine dont hack me... you will steal all my SQL secrets
//
//   5: Download the MySQL JDBC driver .jar file
//           Go to: https://dev.mysql.com/downloads/connector/j/
//           Pick "Platform Independent" → download ZIP → extract it
//           Put the .jar file in a folder called lib/ in your project
//           This is what I have in the lib folder under docs 
//   6: Ran this in the terminal 
//        >> Remove-Item out -Recurse -Force
//        >> New-Item -ItemType Directory -Name out
//        >> javac -cp ".;lib/*" -d out src\backend\listing\*.java
//        >> java -cp ".;lib/*;out" com.commonground.listing.ListingTest
//
// Tested and it all passed the requirments that was in the specifcation 
//   RESULTS: 20 passed, 0 failed
//   ALL TESTS PASSED 


// WHAT EACH TEST COVERS: tried to cover everything
//   Test 01 → (all required fields), REQ-2 (unique ID), REQ-7 (status = Available)
//   Test 02 → (validate: blank item name rejected)
//   Test 03 → (validate: blank description rejected)
//   Test 04 → (validate: zero price rejected)
//   Test 05 → (validate: free-typed category rejected — added the dropdown function made sure they cant just free enter)
//   Test 06 → (validate: invalid condition rejected)
//   Test 07 → (read listing back, verify all fields)
//   Test 08 → (status displayed correctly)
//   Test 09 → (edit listing, update fields successfully)
//   Test 10 → (cannot edit a Sold listing)
//   Test 11 → (cannot edit a Reserved listing)
//   Test 12 → (can delete an Available listing — soft delete)
//   Test 13 → (cannot delete a Sold listing)
//   Test 14 → (status: Available → Reserved → Sold transitions)
//   Test 15 → Image: add image file path, retrieve it
//   Test 16 → Search by keyword
//   Test 17 → Filter by category
//   Test 18 → Seller's "My Listings" view
//   Test 19 → Category enum validation
//   Test 20 → Price display formatting

import java.util.List;

public class ListingTest {

   
    // SERVICE — this is what we test. It internally uses the DAO.
    // Testing the service = testing service + DAO + database all at once.
    
    private static final ListingService service = new ListingService();

   
   
    // These match the IDs we inserted in listing_setup.sql.
    //pulls to check valaidations 
    // TEST_CLIENT_ID = 1   (the test seller account we created)
    // TEST_LOCATION_ID = 1 (the test Walmart location we created)
   
    private static final int TEST_CLIENT_ID   = 1;
    private static final int TEST_LOCATION_ID = 1;

    // Counters — updated automatically by pass() and fail() methods
    private static int passCount = 0;
    private static int failCount = 0;


    //tests begin 
    public static void main(String[] args) {

        printLine("=", 52);
        System.out.println("  CommonGround — Listing Tests | Adriana");
        System.out.println("  COSC 4319 | WeJustTrynaGraduate (WJTG)");
        printLine("=", 52);
        System.out.println();

        //gets categories 
        System.out.println("SETUP: Seeding categories into DB...");
        service.initialize();
        List<String[]> cats = service.getAllCategories();
        System.out.println("       " + cats.size() + " categories loaded.\n");
        

        // Run every test. Some tests return a listing ID that later tests use.
        int listingId = test01_CreateValidListing();
        test02_RejectBlankItemName();
        test03_RejectBlankDescription();
        test04_RejectZeroPrice();
        test05_RejectFreeTypedCategory();
        test06_RejectInvalidCondition();
        test07_ReadListingBackFromDB(listingId);
        test08_StatusDisplayedCorrectly(listingId);
        test09_EditListing(listingId);
        test10_CannotEditSoldListing();
        test11_CannotEditReservedListing();
        test12_DeleteAvailableListing();
        test13_CannotDeleteSoldListing();
        test14_StatusTransitions(listingId);
        test15_AddAndRetrieveImagePath(listingId);
        test16_SearchByKeyword();
        test17_FilterByCategory();
        test18_GetMyListings();
        test19_CategoryEnumValidation();
        test20_PriceDisplay();

       //summary at the end 
        System.out.println();
        printLine("=", 52);
        System.out.printf("  RESULTS: %d passed, %d failed%n", passCount, failCount);
        printLine("=", 52);
        if (failCount == 0) {
            System.out.println("  ALL TESTS PASSED — Listing feature is ready!");
        } else {
            System.out.println("  SOME TESTS FAILED — see FAIL messages above.");
            System.out.println("  Fix those issues before connecting to the UI team.");
        }
        printLine("=", 52);
    }


    private static int test01_CreateValidListing() {
        System.out.println("TEST 01: Create a valid listing ");

        ServiceResult result = service.createListing(
            TEST_CLIENT_ID,          // who is posting (the seller)
            TEST_LOCATION_ID,        // where to meet
            "Phones & Tablets",      // category — from the approved dropdown
            "iPhone 13 128GB Space Gray", // item name
            299.99,                  // price: $299.99
            "Used for 1 year. Battery health 94%. Comes with original box and charger. No cracks.",
            "Good",                  // condition
            "App Payment"            // payment type
        );

        if (result.isSuccess() && result.getId() > 0) {
            // Also verify the listing was actually saved to DB with correct status
            Listing saved = service.getListingById(result.getId());
            if (saved != null && Listing.STATUS_AVAILABLE.equals(saved.getStatus())) {
                pass("TEST 01",
                     "Created listing ID=" + result.getId() +
                     ": " + saved.getItemName() +
                     ", " + saved.getPriceDisplay() +
                     ", status=" + saved.getStatus());
                return result.getId(); // return ID for use in later tests
            } else {
                fail("TEST 01", "Listing was inserted but status is wrong or listing not found in DB.");
                return -1;
            }
        } else {
            fail("TEST 01", "Expected success but got: " + result.getMessage());
            return -1;
        }
    }


    private static void test02_RejectBlankItemName() {
        System.out.println("TEST 02: Reject blank item name ");

        ServiceResult result = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Electronics",
            "",           // ← blank item name should be rejected
            50.00,
            "Some description",
            "Good",
            "App Payment"
        );

        if (!result.isSuccess() && result.getMessage().contains("Item name")) {
            pass("TEST 02", "Correctly rejected. Error: '" + result.getMessage() + "'");
        } else {
            fail("TEST 02",
                 "Should have rejected blank item name. Got: " + result.getMessage());
        }
    }


  
    private static void test03_RejectBlankDescription() {
        System.out.println("TEST 03: Reject blank description");

        ServiceResult result = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Furniture & Home Décor",
            "Wooden Desk",
            75.00,
            "   ",         // ← whitespace only — should count as blank
            "Good",
            "Cash"
        );

        if (!result.isSuccess() && result.getMessage().contains("Description")) {
            pass("TEST 03", "Correctly rejected blank description.");
        } else {
            fail("TEST 03",
                 "Should have rejected blank description. Got: " + result.getMessage());
        }
    }


    private static void test04_RejectZeroPrice() {
        System.out.println("TEST 04: Reject price = $0.00 ");

        ServiceResult result = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Books & Magazines",
            "Free Book",
            0.0,           // ← price is zero should be rejected
            "Good condition",
            "Like New",
            "Cash"
        );

        if (!result.isSuccess() && result.getMessage().contains("Price")) {
            pass("TEST 04", "Correctly rejected zero price.");
        } else {
            fail("TEST 04",
                 "Should have rejected price=0. Got: " + result.getMessage());
        }
    }



    private static void test05_RejectFreeTypedCategory() {
        System.out.println("TEST 05: Reject free-typed category ");

        ServiceResult result = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "My Own Made Up Category", // ← NOT in the approved list should be rejected
            "Some Item",
            20.00,
            "Item description",
            "Good",
            "Cash"
        );

        if (!result.isSuccess() && result.getMessage().contains("category")) {
            pass("TEST 05",
                 "Correctly rejected free-typed category. Error: '" + result.getMessage() + "'");
        } else {
            fail("TEST 05",
                 "Should have blocked non-dropdown category. Got: " + result.getMessage());
        }
    }


 

    private static void test06_RejectInvalidCondition() {
        System.out.println("TEST 06: Reject invalid condition ");

        ServiceResult result = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Gaming",
            "PS5 Console",
            400.00,
            "Barely used",
            "Almost New",  // ← NOT on the approved list 
            "App Payment"
        );

        if (!result.isSuccess() && result.getMessage().contains("condition")) {
            pass("TEST 06", "Correctly rejected invalid condition.");
        } else {
            fail("TEST 06",
                 "Should have rejected invalid condition. Got: " + result.getMessage());
        }
    }


  
    private static void test07_ReadListingBackFromDB(int listingId) {
        System.out.println("TEST 07: Read listing from DB and verify fields ");

        if (listingId <= 0) {
            fail("TEST 07", "Skipped — Test 01 did not produce a valid listing ID.");
            return;
        }

        Listing l = service.getListingById(listingId);

        if (l == null) {
            fail("TEST 07", "Listing with ID=" + listingId + " not found in database.");
            return;
        }

        // Check every field we saved matches what we get back
        boolean itemNameOk    = "iPhone 13 128GB Space Gray".equals(l.getItemName());
        boolean priceOk       = l.getPrice() == 299.99;
        boolean categoryOk    = "Phones & Tablets".equals(l.getCategoryName());
        boolean conditionOk   = "Good".equals(l.getItemCondition());
        boolean paymentOk     = "App Payment".equals(l.getPaymentType());
        boolean statusOk      = Listing.STATUS_AVAILABLE.equals(l.getStatus());
        boolean clientOk      = l.getClientId() == TEST_CLIENT_ID;

        if (itemNameOk && priceOk && categoryOk && conditionOk
                && paymentOk && statusOk && clientOk) {
            pass("TEST 07", "All fields verified: " + l);
        } else {
            fail("TEST 07",
                 "Field mismatch! itemName=" + itemNameOk +
                 " price=" + priceOk +
                 " category=" + categoryOk +
                 " condition=" + conditionOk +
                 " payment=" + paymentOk +
                 " status=" + statusOk +
                 " client=" + clientOk +
                 " | Actual: " + l);
        }
    }


  
    private static void test08_StatusDisplayedCorrectly(int listingId) {
        System.out.println("TEST 08: Status displayed as text ");

        if (listingId <= 0) {
            fail("TEST 08", "Skipped — no valid listing ID.");
            return;
        }

        Listing l = service.getListingById(listingId);

        if (l == null) {
            fail("TEST 08", "Listing not found.");
            return;
        }

        // status should be the text "Available" not a number like 0
        boolean isTextStatus = Listing.STATUS_AVAILABLE.equals(l.getStatus());
        boolean isNotNumber  = !l.getStatus().matches("\\d+"); // no digits

        if (isTextStatus && isNotNumber) {
            pass("TEST 08",
                 "Status is text '" + l.getStatus() +
                 "' (not a number).  satisfied.");
        } else {
            fail("TEST 08", "Status should be 'Available' text. Got: '" + l.getStatus() + "'");
        }
    }


   
    private static void test09_EditListing(int listingId) {
        System.out.println("TEST 09: Edit an Available listing");

        if (listingId <= 0) {
            fail("TEST 09", "Skipped — no valid listing ID.");
            return;
        }

        // Update: lower the price, change the description, change condition
        ServiceResult result = service.editListing(
            listingId,
            TEST_CLIENT_ID,
            "Phones & Tablets",             // same category
            "iPhone 13 128GB — PRICE DROP!", // updated name
            249.99,                          // price dropped from $299.99
            "Price reduced! Great condition. Battery 94%. Original charger included.",
            "Like New",                      // condition updated
            "App Payment"
        );

        if (result.isSuccess()) {
            // Confirm the change was actually saved to DB
            Listing updated = service.getListingById(listingId);
            boolean nameUpdated  = updated != null && updated.getItemName().contains("PRICE DROP");
            boolean priceUpdated = updated != null && updated.getPrice() == 249.99;
            boolean condUpdated  = updated != null && "Like New".equals(updated.getItemCondition());

            if (nameUpdated && priceUpdated && condUpdated) {
                pass("TEST 09",
                     "Edit confirmed in DB: '" + updated.getItemName() +
                     "' | " + updated.getPriceDisplay() +
                     " | " + updated.getItemCondition());
            } else {
                fail("TEST 09",
                     "Edit said success but DB data does not match expectations.");
            }
        } else {
            fail("TEST 09", "Edit failed: " + result.getMessage());
        }
    }


    private static void test10_CannotEditSoldListing() {
        System.out.println("TEST 10: Cannot edit a Sold listing");

        // Create a listing and immediately mark it Sold
        ServiceResult cr = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Sports & Outdoors", "Temp Item for Sold Test",
            30.00, "Testing edit block on Sold listing.", "Fair", "Cash"
        );
        if (!cr.isSuccess()) { fail("TEST 10", "Could not create test listing."); return; }

        int tempId = cr.getId();
        service.updateStatus(tempId, TEST_CLIENT_ID, Listing.STATUS_SOLD);

        // Now try to edit it should be rejected
        ServiceResult editResult = service.editListing(
            tempId, TEST_CLIENT_ID,
            "Electronics", "Hacked Edit Attempt",
            999.99, "This should not save.", "New", "App Payment"
        );

        if (!editResult.isSuccess() && editResult.getMessage().contains("Sold")) {
            pass("TEST 10", "Correctly blocked edit of Sold listing. Msg: '" + editResult.getMessage() + "'");
        } else {
            fail("TEST 10",
                 "Should have blocked edit of Sold listing. Got: " + editResult.getMessage());
        }
    }


    private static void test11_CannotEditReservedListing() {
        System.out.println("TEST 11: Cannot edit a Reserved listing");

        ServiceResult cr = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Automotive", "Temp Item for Reserved Test",
            500.00, "Testing edit block while Reserved.", "Good", "Cash"
        );
        if (!cr.isSuccess()) { fail("TEST 11", "Could not create test listing."); return; }

        int tempId = cr.getId();
        service.updateStatus(tempId, TEST_CLIENT_ID, Listing.STATUS_RESERVED);

        ServiceResult editResult = service.editListing(
            tempId, TEST_CLIENT_ID,
            "Electronics", "Edit While Reserved",
            100.00, "Should not work.", "New", "App Payment"
        );

        if (!editResult.isSuccess()) {
            pass("TEST 11",
                 "Correctly blocked edit of Reserved listing. Msg: '" + editResult.getMessage() + "'");
        } else {
            fail("TEST 11", "Should have blocked edit of Reserved listing.");
        }
    }



    private static void test12_DeleteAvailableListing() {
        System.out.println("TEST 12: Delete an Available listing → soft delete ");

        ServiceResult cr = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Baby & Kids", "Old Baby Clothes Bundle",
            15.00, "Assorted baby clothes, sizes 0-6 months. Good condition.", "Good", "Cash"
        );
        if (!cr.isSuccess()) { fail("TEST 12", "Could not create test listing."); return; }

        int tempId = cr.getId();

        ServiceResult deleteResult = service.deleteListing(tempId, TEST_CLIENT_ID);

        if (deleteResult.isSuccess()) {
            // Verify: the row still exists but status is now "Removed"
            Listing deleted = service.getListingById(tempId);
            if (deleted != null && Listing.STATUS_REMOVED.equals(deleted.getStatus())) {
                pass("TEST 12",
                     "Listing ID=" + tempId +
                     " is now status='" + deleted.getStatus() +
                     "' (row still exists — soft delete).");
            } else {
                fail("TEST 12",
                     "Delete said success but status is not 'Removed'. Status: " +
                     (deleted == null ? "null" : deleted.getStatus()));
            }
        } else {
            fail("TEST 12", "Delete failed: " + deleteResult.getMessage());
        }
    }



    private static void test13_CannotDeleteSoldListing() {
        System.out.println("TEST 13: Cannot delete a Sold listing ");

        ServiceResult cr = service.createListing(
            TEST_CLIENT_ID, TEST_LOCATION_ID,
            "Toys & Games", "Board Game Lot",
            40.00, "5 board games in great condition.", "Good", "Cash"
        );
        if (!cr.isSuccess()) { fail("TEST 13", "Could not create test listing."); return; }

        int tempId = cr.getId();
        service.updateStatus(tempId, TEST_CLIENT_ID, Listing.STATUS_SOLD);

        ServiceResult deleteResult = service.deleteListing(tempId, TEST_CLIENT_ID);

        if (!deleteResult.isSuccess() && deleteResult.getMessage().contains("sold")) {
            pass("TEST 13", "Correctly blocked delete of Sold listing.");
        } else {
            fail("TEST 13",
                 "Should have blocked deleting a Sold listing. Got: " + deleteResult.getMessage());
        }
    }


    private static void test14_StatusTransitions(int listingId) {
        System.out.println("TEST 14: Status transitions Available→Reserved→Sold ");

        if (listingId <= 0) {
            fail("TEST 14", "Skipped — no valid listing ID.");
            return;
        }

       
        service.updateStatus(listingId, TEST_CLIENT_ID, Listing.STATUS_AVAILABLE);

        // ── Step 1: Buyer initiates purchase → Reserved ──────────────────
        ServiceResult r1 = service.updateStatus(
            listingId, TEST_CLIENT_ID, Listing.STATUS_RESERVED
        );
        Listing l1 = service.getListingById(listingId);
        boolean step1ok = r1.isSuccess()
                && l1 != null
                && Listing.STATUS_RESERVED.equals(l1.getStatus());

        if (step1ok) {
            System.out.println("   Step 1: Available → Reserved ✓");
        } else {
            fail("TEST 14", "Could not set to Reserved. Msg: " + r1.getMessage());
            return;
        }

        //both parties confirm each 
        ServiceResult r2 = service.updateStatus(
            listingId, TEST_CLIENT_ID, Listing.STATUS_SOLD
        );
        Listing l2 = service.getListingById(listingId);
        boolean step2ok = r2.isSuccess()
                && l2 != null
                && Listing.STATUS_SOLD.equals(l2.getStatus());

        if (step2ok) {
            System.out.println("   Step 2: Reserved → Sold ✓");
            pass("TEST 14",
                 "Status flow complete. Final status: '" + l2.getStatus() + "'");
        } else {
            fail("TEST 14", "Could not set to Sold. Msg: " + r2.getMessage());
        }
    }


    private static void test15_AddAndRetrieveImagePath(int listingId) {
        System.out.println("TEST 15: Add and retrieve image file path ");

        if (listingId <= 0) {
            fail("TEST 15", "Skipped — no valid listing ID.");
            return;
        }

        // Simulate what would happen after a user uploads a photo:
        // The servlet saves the file to disk then calls this with the path.
        String fakeFileName = "listing_" + listingId + "_photo1.jpg";
        String fakePath     = "/uploads/listings/listing_" + listingId + "_photo1.jpg";

        ServiceResult addResult = service.addImage(listingId, fakeFileName, fakePath);

        if (!addResult.isSuccess()) {
            fail("TEST 15", "addImage() failed: " + addResult.getMessage());
            return;
        }

        // Now retrieve the images and verify our entry is there
        List<String[]> images = service.getImages(listingId);

        boolean found = false;
        for (String[] img : images) {
            // img[0] = file_name, img[1] = file_path
            if (fakeFileName.equals(img[0]) && fakePath.equals(img[1])) {
                found = true;
                break;
            }
        }

        if (found) {
            pass("TEST 15",
                 "Image path saved and retrieved: " +
                 images.get(0)[0] + " → " + images.get(0)[1]);
        } else {
            fail("TEST 15",
                 "Image was saved but not found in getImages() results.");
        }
    }


    
    private static void test16_SearchByKeyword() {
        System.out.println("TEST 16: Search listings by keyword");

        // Search for "iPhone" we created an iPhone listing in Test 01
        List<Listing> results = service.search("iPhone");

        if (results != null) {
            pass("TEST 16",
                 "Search for 'iPhone' returned " + results.size() +
                 " result(s). (Note: only Available listings appear.)");
        } else {
            fail("TEST 16", "search() returned null instead of a list.");
        }
    }


    
    private static void test17_FilterByCategory() {
        System.out.println("TEST 17: Filter listings by category");

        // Valid category should return a list (may be empty if all sold/removed)
        List<Listing> valid = service.getListingsByCategory("Electronics");
        if (valid == null) {
            fail("TEST 17", "getListingsByCategory() returned null for a valid category.");
            return;
        }
        System.out.println("   Electronics has " + valid.size() + " Available listing(s).");

        // Invalid category should return empty list, NOT null
        List<Listing> invalid = service.getListingsByCategory("Not A Real Category");
        if (invalid != null && invalid.isEmpty()) {
            pass("TEST 17",
                 "Valid category returned list (" + valid.size() + " items). " +
                 "Invalid category correctly returned empty list.");
        } else {
            fail("TEST 17",
                 "Invalid category should return empty list. Got: " +
                 (invalid == null ? "null" : invalid.size() + " items"));
        }
    }


    
    private static void test18_GetMyListings() {
        System.out.println("TEST 18: Get seller's own listings (My Listings page)");

        List<Listing> myListings = service.getMyListings(TEST_CLIENT_ID);

        if (myListings != null) {
            pass("TEST 18",
                 "getMyListings() returned " + myListings.size() +
                 " listing(s) for client_id=" + TEST_CLIENT_ID +
                 " (excludes Removed ones).");
        } else {
            fail("TEST 18", "getMyListings() returned null.");
        }
    }


   
    private static void test19_CategoryEnumValidation() {
        System.out.println("TEST 19: Category enum validation ");

        // These should all be VALID (in the approved list)
        boolean v1 = ListingCategory.isValid("Electronics");
        boolean v2 = ListingCategory.isValid("Phones & Tablets");
        boolean v3 = ListingCategory.isValid("Men's Clothing");
        boolean v4 = ListingCategory.isValid("Health & Wellness");
        boolean v5 = ListingCategory.isValid("Pet Supplies");
        boolean v6 = ListingCategory.isValid("Art & Collectibles");
        boolean v7 = ListingCategory.isValid("Other");

        // These should all be INVALID (NOT in the approved list)
        boolean i1 = ListingCategory.isValid("Random Stuff");          // made up
        boolean i2 = ListingCategory.isValid("electronics");           // wrong case
        boolean i3 = ListingCategory.isValid("");                      // empty
        boolean i4 = ListingCategory.isValid(null);                    // null
        boolean i5 = ListingCategory.isValid("  Electronics  ");       // extra spaces

        boolean allValid   = v1 && v2 && v3 && v4 && v5 && v6 && v7;
        boolean allInvalid = !i1 && !i2 && !i3 && !i4 && !i5;

        // Also check that getAllDisplayNames() returns the right count
        String[] allNames = ListingCategory.getAllDisplayNames();
        boolean correctCount = allNames.length == 27; // 27 categories in the enum

        if (allValid && allInvalid && correctCount) {
            pass("TEST 19",
                 "All 27 categories validated correctly. " +
                 "Valid inputs accepted. Invalid inputs rejected.");
        } else {
            fail("TEST 19",
                 "Validation issue! " +
                 "allValid=" + allValid + " allInvalid=" + allInvalid +
                 " count=" + allNames.length + " (expected 27)");
        }
    }


   
    private static void test20_PriceDisplay() {
        System.out.println("TEST 20: Price formats correctly as dollar string");

        Listing l = new Listing();

        l.setPrice(299.99);
        boolean t1 = "$299.99".equals(l.getPriceDisplay());

        l.setPrice(10.99);
        boolean t2 = "$10.99".equals(l.getPriceDisplay());

        l.setPrice(5.00);
        boolean t3 = "$5.00".equals(l.getPriceDisplay());

        l.setPrice(0.50);
        boolean t4 = "$0.50".equals(l.getPriceDisplay());

        l.setPrice(1000.00);
        boolean t5 = "$1000.00".equals(l.getPriceDisplay());

        if (t1 && t2 && t3 && t4 && t5) {
            pass("TEST 20",
                 "Prices format correctly: $299.99, $10.99, $5.00, $0.50, $1000.00");
        } else {
            fail("TEST 20",
                 "Price formatting issue. t1=" + t1 + " t2=" + t2 +
                 " t3=" + t3 + " t4=" + t4 + " t5=" + t5);
        }
    }


//helper methods prints 

 
    private static void pass(String name, String message) {
        System.out.println("   ✓ PASS — " + message);
        System.out.println();
        passCount++;
    }

 
    private static void fail(String name, String message) {
        System.out.println("   ✗ FAIL — " + message);
        System.out.println();
        failCount++;
    }

    
    private static void printLine(String ch, int count) {
        System.out.println(ch.repeat(count));
    }
}
