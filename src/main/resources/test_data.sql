USE CommonGround_db;

-- Insert test accounts with address in the account table
INSERT IGNORE INTO account (account_id, username, password_hash, email, address)
VALUES (901, 'testbuyer', 'testpass', 'buyer@test.com', '6000 Teague Rd Houston TX');

INSERT IGNORE INTO account (account_id, username, password_hash, email, address)
VALUES (902, 'testseller', 'testpass', 'seller@test.com', '8300 Mykawa Rd Houston TX');

-- Insert client records (just account_id, permissions)
INSERT IGNORE INTO client (account_id, can_list, can_purchase)
VALUES (901, false, true);

INSERT IGNORE INTO client (account_id, can_list, can_purchase)
VALUES (902, true, false);

-- Insert a test listing (required by transaction)
INSERT IGNORE INTO listing (listing_id, client_id, location_id, category_id, item_name, price, status)
VALUES (1, 902, 1, 1, 'Test Item', 10.00, 'active');

-- Insert the test transaction
INSERT IGNORE INTO transactions (transaction_id, buyer_id, seller_id, listing_id, status)
VALUES (1, 901, 902, 1, 'pending');

-- Verify
SELECT t.transaction_id, a1.address AS buyer_address, a2.address AS seller_address, t.meetup_address
FROM transactions t
JOIN account a1 ON t.buyer_id = a1.account_id
JOIN account a2 ON t.seller_id = a2.account_id
WHERE t.transaction_id = 1;
