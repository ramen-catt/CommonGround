USE CommonGround_db;

-- admin account
INSERT IGNORE INTO account
    (account_id, username, password_hash, phone_number, address, email, is_admin, is_suspended)
VALUES
    (2,
     'adriana_admin',
     '$2b$10$placeholder_hash_for_testing_only',
     '5559999999',
     '456 Admin Street, Houston TX 77001',
     'adriana_admin@commonground.com',
     TRUE,
     FALSE);

-- regular user
INSERT IGNORE INTO account
    (account_id, username, password_hash, phone_number, address, email, is_admin, is_suspended)
VALUES
    (3,
     'test_regular_user',
     '$2b$10$placeholder_hash_for_testing_only',
     '5558888888',
     '789 User Street, Houston TX 77001',
     'regular_user@commonground.com',
     FALSE,
     FALSE);

-- make sure client rows exist
INSERT IGNORE INTO client (account_id, can_list, can_purchase)
VALUES
    (1, TRUE, TRUE),
    (3, TRUE, TRUE);

-- make sure location 1 exists
INSERT IGNORE INTO location
    (location_id, address, latitude, longitude, name, vetted)
VALUES
    (1,
     'Test Meetup Spot, Huntsville TX',
     30.7235,
     -95.5508,
     'Test Safe Spot',
     TRUE);

-- make sure category 1 exists
INSERT IGNORE INTO category
    (category_id, category_name)
VALUES
    (1, 'Test Category');

-- remove old test feedback first in case it exists
DELETE FROM feedback WHERE feedback_id = 999;

-- remove old test listing first in case it exists
DELETE FROM listing WHERE listing_id = 999;

-- create listing that AdminTest expects
INSERT INTO listing
    (listing_id, client_id, location_id, category_id, payment_type,
     item_name, price, description, item_condition, status)
VALUES
    (999,
     1,
     1,
     1,
     'Cash',
     'Admin Test Item',
     10.00,
     'This listing is used to test admin remove listing.',
     'Good',
     'Available');

-- create feedback/report that AdminTest expects
INSERT INTO feedback
    (feedback_id, buyer_id, seller_id, listing_id, rating,
     rating_report, rating_desc, review_desc)
VALUES
    (999,
     1,
     1,
     999,
     2,
     TRUE,
     'Suspicious item',
     'Item did not match description, possible scam.');

-- verify
SELECT account_id, username, is_admin, is_suspended
FROM account
WHERE account_id IN (1,2,3);

SELECT account_id, can_list, can_purchase
FROM client
WHERE account_id IN (1,3);

SELECT location_id, name, address
FROM location
WHERE location_id = 1;

SELECT category_id, category_name
FROM category
WHERE category_id = 1;

SELECT listing_id, client_id, location_id, category_id, status
FROM listing
WHERE listing_id = 999;

SELECT feedback_id, buyer_id, seller_id, listing_id, rating_report
FROM feedback
WHERE feedback_id = 999;