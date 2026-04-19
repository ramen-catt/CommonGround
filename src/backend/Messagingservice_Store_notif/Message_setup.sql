use CommonGround;

Insert IGNORE INTO message(
    conversation_id, sender_id, receiver_id, message_text
)VALUES
    (1, 1, 2, "Hi I am testing the messaging service"),
    (1, 1, 2, "Hello! This is a message from sender 1 to receiver 2"),
    (1, 2, 1, "Hi! This is a message from sender 2 to receiver 1"),
    (2, 1, 3, "Hey there! This is a message from sender 1 to receiver 3"),
    (3, 1, "Hello! This is a message from sender 3 to receiver 1");

CREATE TABLE conversation (
    conversation_id INT AUTO_INCREMENT PRIMARY KEY,
    participant_1   INT NOT NULL,
    participant_2   INT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_p1
        FOREIGN KEY (participant_1) REFERENCES client(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_conv_p2
        FOREIGN KEY (participant_2) REFERENCES client(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO account (username, password_hash, email) VALUES
('user2', 'hashedpassword', 'user2@email.com'),
('user3', 'hashedpassword', 'user3@email.com');

INSERT INTO client (account_id, can_list, can_purchase) VALUES
(1, TRUE, TRUE),
(2, TRUE, TRUE);

INSERT INTO message (conversation_id, sender_id, receiver_id, message_text)
VALUES (1, 1, 2, 'hello');
SELECT * FROM message

--add this to message table
CONSTRAINT fk_message_conversation
    FOREIGN KEY (conversation_id) REFERENCES conversation(conversation_id)
    ON DELETE CASCADE ON UPDATE CASCADE


------ENTIRE SETUP THING TO COPY AND PASTE ONTO LAPTOP
--====================================================
--++++++++++++++++++++++++++++++++++++++++++++++++++++

DROP DATABASE IF EXISTS CommonGround_db;
CREATE DATABASE CommonGround_db;
USE CommonGround_db;

DROP TABLE IF EXISTS audit_trail;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS message_image;
DROP TABLE IF EXISTS listing_image;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS listing;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
account_id INT auto_increment PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
phone_number VARCHAR(20),
address VARCHAR(255),
email VARCHAR(100) NOT NULL UNIQUE,
is_admin BOOLEAN DEFAULT FALSE,
is_suspended BOOLEAN DEFAULT FALSE
);

CREATE TABLE client (
    account_id INT PRIMARY KEY,
    can_list BOOLEAN DEFAULT FALSE,
    can_purchase BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_client_account
        FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE location (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    name VARCHAR(100),
    vetted BOOLEAN DEFAULT FALSE
);

CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE listing (
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

CREATE TABLE listing_image (
    listing_image_id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_image_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE conversation (
    conversation_id INT AUTO_INCREMENT PRIMARY KEY,
    participant_1   INT NOT NULL,
    participant_2   INT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_p1
        FOREIGN KEY (participant_1) REFERENCES client(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_conv_p2
        FOREIGN KEY (participant_2) REFERENCES client(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE message (
	conversation_id INT NOT NULL,
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
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
	CONSTRAINT fk_message_conversation
		FOREIGN KEY (conversation_id) REFERENCES conversation(conversation_id)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE message_image (
    message_image_id INT AUTO_INCREMENT PRIMARY KEY,
    message_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_image_message
        FOREIGN KEY (message_id) REFERENCES message(message_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE transactions (
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

CREATE TABLE audit_trail (
    audit_id        INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id  INT          NULL,
    listing_id      INT          NULL,
    client_id       INT          NULL,
    action          VARCHAR(50)  NULL,
    new_status      VARCHAR(20)  NULL,
    event_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,
    listing_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    rating_report BOOLEAN DEFAULT FALSE,
    rating_desc VARCHAR(255),
    review_desc TEXT,
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
INSERT INTO account (username, password_hash, email) VALUES
('user2', 'hashedpassword', 'user2@email.com'),
('user3', 'hashedpassword', 'user3@email.com');

INSERT INTO client (account_id, can_list, can_purchase) VALUES
(1, TRUE, TRUE),
(2, TRUE, TRUE);

INSERT INTO conversation (participant_1, participant_2) VALUES
(1, 2);

INSERT INTO message (conversation_id, sender_id, receiver_id, message_text)
VALUES (1, 1, 2, 'hello');
SELECT * FROM conversation