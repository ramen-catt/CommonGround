-- Safe setup/update script.
-- This keeps existing users, listings, messages, and transactions.
-- Do not add DROP DATABASE or DROP TABLE here unless you intentionally want to erase data.

CREATE DATABASE IF NOT EXISTS CommonGround_db;
USE CommonGround_db;

CREATE TABLE IF NOT EXISTS account (
account_id INT auto_increment PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
phone_number VARCHAR(20),
address VARCHAR(255),
email VARCHAR(100) NOT NULL UNIQUE,
is_admin BOOLEAN DEFAULT FALSE,
is_suspended BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add created_at to existing installs that ran before this column was added
ALTER TABLE account ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS client (
    account_id INT PRIMARY KEY,
    can_list BOOLEAN DEFAULT FALSE,
    can_purchase BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_client_account
        FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Demo accounts for classmates/testing.
-- tester1@gmail.com / tester1
-- tester2@gamil.com / tester2
-- admin@commonground.com / admin
-- adriana_admin@commonground.com / admin
SET @tester1_pw = '7abcddbb2c74e4c0789c2c0aa6abcf5172e82e9f4916bc6409fc3989ed673e08';
SET @tester2_pw = '7cd477192d54ceb8673be093f443b8622c612896880f6879c7f8ec16fa7ba114';
SET @admin_pw = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';

INSERT INTO account (username, password_hash, phone_number, address, email, is_admin, is_suspended)
VALUES
('tester1',       @tester1_pw, '7135550101', 'Houston, TX', 'tester1@gmail.com', FALSE, FALSE),
('tester2',       @tester2_pw, '7135550102', 'Houston, TX', 'tester2@gamil.com', FALSE, FALSE),
('admin',         @admin_pw,   '5550000000', 'Houston, TX', 'admin@commonground.com', TRUE, FALSE),
('adriana_admin', @admin_pw,   '5559999999', 'Houston, TX', 'adriana_admin@commonground.com', TRUE, FALSE)
ON DUPLICATE KEY UPDATE
password_hash = VALUES(password_hash),
email = VALUES(email),
is_admin = VALUES(is_admin),
is_suspended = VALUES(is_suspended);

INSERT INTO client (account_id, can_list, can_purchase)
SELECT account_id, TRUE, TRUE
FROM account
WHERE username IN ('tester1', 'tester2')
ON DUPLICATE KEY UPDATE
can_list = VALUES(can_list),
can_purchase = VALUES(can_purchase);

CREATE TABLE IF NOT EXISTS location (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    name VARCHAR(100),
    vetted BOOLEAN DEFAULT FALSE
);

INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '1200 Travis St Houston TX', 29.75568, -95.36739, 'HPD Headquarter', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '1200 Travis St Houston TX');
INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '7525 Sherman St Houston TX', 29.73425, -95.29035, 'HPD Magnolia Park', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '7525 Sherman St Houston TX');
INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '5600 S Willow Dr 116 Houston TX', 29.65195, -95.47672, 'HPD Westbury', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '5600 S Willow Dr 116 Houston TX');
INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '6000 Teague Rd Houston TX', 29.85814, -95.53938, 'HPD Northwest', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '6000 Teague Rd Houston TX');
INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '3203 Dairy Ashford Rd Houston TX', 29.74083, -95.60423, 'HPD Westside', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '3203 Dairy Ashford Rd Houston TX');
INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '9455 W Montgomery Rd Houston TX', 29.88620, -95.44561, 'HPD North', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '9455 W Montgomery Rd Houston TX');
INSERT INTO location (address, latitude, longitude, name, vetted)
SELECT '8300 Mykawa Rd Houston TX', 29.66628, -95.32202, 'HPD Southeast', true
WHERE NOT EXISTS (SELECT 1 FROM location WHERE address = '8300 Mykawa Rd Houston TX');

CREATE TABLE IF NOT EXISTS category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT IGNORE INTO category (category_name)
value
('Clothing'),
('Furniture'),
('Electronics'),
('Automotive'),
('Jewelry'),
('Tools'),
('Home Decor'),
('Office Equipment');

CREATE TABLE IF NOT EXISTS listing (
    listing_id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    location_id INT NOT NULL,
    category_id INT NOT NULL,
    payment_type VARCHAR(50),
    item_name VARCHAR(100),
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    item_condition VARCHAR(50),
    status VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_client
        FOREIGN KEY (client_id) REFERENCES client(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_listing_location
        FOREIGN KEY (location_id) REFERENCES location(location_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_listing_category
        FOREIGN KEY (category_id) REFERENCES category(category_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS listing_image (
    listing_image_id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path MEDIUMTEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_image_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    listing_id INT NULL,
    message_text TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES client(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_message_receiver
        FOREIGN KEY (receiver_id) REFERENCES client(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_message_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,
    listing_id INT NOT NULL,
    meetup_address VARCHAR(255),
    status VARCHAR(50),
    transaction_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_buyer
        FOREIGN KEY (buyer_id) REFERENCES client(account_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_transactions_seller
        FOREIGN KEY (seller_id) REFERENCES client(account_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_transactions_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_trail (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NULL,
    listing_id INT NOT NULL,
    client_id INT NOT NULL,
    action VARCHAR(50),
    new_status VARCHAR(20),
    event_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,
    listing_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    rating_report BOOLEAN DEFAULT FALSE,
    rating_desc VARCHAR(255),
    report_desc TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_buyer
        FOREIGN KEY (buyer_id) REFERENCES client(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_seller
        FOREIGN KEY (seller_id) REFERENCES client(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
