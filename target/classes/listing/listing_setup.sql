--Adriana (Listing – Backend) test file dummy file used in SQL added some stuff to test more futher stuff without other peoeples input
--I ran this after running Dennis/Miguel P database 

--side note I was having issues with my own root account so I created another one below in another query and ran it 
--CREATE USER 'cguser'@'localhost' IDENTIFIED BY 'cgpass123';
--GRANT ALL PRIVILEGES ON commonground_db.* TO 'cguser'@'localhost';
--FLUSH PRIVILEGES;--

USE CommonGround_db;

--Creating an account Miguel V should handle this but in the meantime I used a dummy one

INSERT IGNORE INTO account
    (account_id, username, password_hash, phone_number, address, email)
VALUES
    (1,
     'adriana_test',
     '$2b$10$placeholder_hash_for_testing_only',  -- fake hashed password
     '5551234567',
     '123 Test Street, Houston TX 77001',
     'adriana_test@commonground.com');
-- Would be account ID 1 can check in the database with this command to make sure 
-- SELECT * FROM account WHERE account_id = 1;



-- regular client since this was seperate in the ERD
INSERT IGNORE INTO client
    (account_id, can_list, can_purchase)
VALUES
    (1, TRUE, TRUE);




-- This is where Briannas mapping feature comes in when she connects her Map API it will generate some numbers this would pull to then saveThis is the meetup spot. In the real app, Brianna's Map feature
-- I entered Wtv
INSERT IGNORE INTO location
    (location_id, address, latitude, longitude, name, vetted)
VALUES
    (1,
     'Walmart Supercenter, 4700 Beechnut St, Houston TX 77096',
     29.7100,    
     -95.4700,  
     'Walmart - Beechnut (Vetted)',
     TRUE);       -- vetted True means it was approved as a safe spot 

-- checking if everything is good 
SELECT 'account table:' AS check_table;
SELECT account_id, username, email FROM account;

SELECT 'client table:' AS check_table;
SELECT account_id, can_list, can_purchase FROM client;

SELECT 'location table:' AS check_table;
SELECT location_id, name, vetted FROM location;

SELECT 'category table (before Java runs):' AS check_table;
SELECT COUNT(*) AS category_count FROM category;
-- Gets categories making sure it gets it from ListingCategory

--test laters makes sure if things are passing things this is what I used to see if this would even work...
-- AUDIT TRAIL TABLE (needed for ListingAuditLogger.java and Test 21)
-- Kelly: this is the table your audit trail feature will also write to
-- You can ADD columns here later for transaction_id etc. without breaking anything

USE CommonGround_db;
DESCRIBE audit_trail;
DROP TABLE IF EXISTS audit_trail;
CREATE TABLE audit_trail (
    audit_id    INT AUTO_INCREMENT PRIMARY KEY,
    listing_id  INT         NULL,
    client_id   INT         NULL,
    action      VARCHAR(50) NULL,
    new_status  VARCHAR(20) NULL,
    timestamp   VARCHAR(50) NULL
);
 
-- Verify it worked
DESCRIBE audit_trail;
SELECT 'audit_trail table ready!' AS status;
  
CREATE TABLE IF NOT EXISTS audit_trail (
    audit_id    INT AUTO_INCREMENT PRIMARY KEY,
    listing_id  INT         NOT NULL,
    client_id   INT         NOT NULL,
    action      VARCHAR(50) NOT NULL,
    new_status  VARCHAR(20) NOT NULL,
    timestamp   VARCHAR(50) NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES listing(listing_id),
    FOREIGN KEY (client_id)  REFERENCES client(account_id)
);
 
SELECT 'audit_trail table:' AS check_table;
SELECT COUNT(*) AS audit_row_count FROM audit_trail;
