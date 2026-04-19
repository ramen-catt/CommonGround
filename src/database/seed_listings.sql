-- ============================================================
--  Common Ground — Full Seed Script
--  Includes: categories, location, accounts, listings, images
--  Run this in MySQL Workbench against CommonGround_db
--  Safe to re-run (uses INSERT IGNORE throughout)
-- ============================================================

USE CommonGround_db;
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- ─────────────────────────────────────────
--  CLEAR OLD SEED DATA (fresh start)
-- ─────────────────────────────────────────
DELETE FROM listing_image;
DELETE FROM listing;
DELETE FROM client WHERE account_id IN (SELECT account_id FROM account WHERE (email LIKE '%@commonground.com' OR email LIKE '%@gmail.com' OR email LIKE '%@yahoo.com' OR email LIKE '%@outlook.com') AND is_admin = FALSE);
DELETE FROM account WHERE (email LIKE '%@commonground.com' OR email LIKE '%@gmail.com' OR email LIKE '%@yahoo.com' OR email LIKE '%@outlook.com') AND is_admin = FALSE;

-- ─────────────────────────────────────────
--  CATEGORIES
-- ─────────────────────────────────────────
INSERT IGNORE INTO category (category_name) VALUES
("Men's Clothing"),("Women's Clothing"),("Kids' Clothing"),
("Shoes & Footwear"),("Bags & Accessories"),("Jewelry & Watches"),
("Electronics"),("Phones & Tablets"),("Computers & Laptops"),
("Gaming"),("Cameras & Photography"),("Furniture"),("Appliances"),
("Kitchen & Dining"),("Tools & Hardware"),("Books & Magazines"),
("Music & Instruments"),("Movies & TV"),("Sports & Outdoors"),
("Toys & Games"),("Art & Collectibles"),("Baby & Kids"),
("Beauty & Personal Care"),("Health & Wellness"),("Automotive"),
("Pet Supplies"),("Home Decor"),("Office Equipment"),("Other");

-- ─────────────────────────────────────────
--  LOCATION
-- ─────────────────────────────────────────
INSERT IGNORE INTO location (location_id, name, address, latitude, longitude)
VALUES (1,'University of Houston','4800 Calhoun Rd, Houston, TX 77004',29.7199,-95.3422);

-- ─────────────────────────────────────────
--  ACCOUNTS
--  Main demo logins for testing:
--    tester1@gmail.com              / tester1
--    tester2@gamil.com              / tester2
--    admin@commonground.com         / admin
--    adriana_admin@commonground.com / admin
--  Other sample users keep password: password123
-- ─────────────────────────────────────────
SET @pw = 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f';
SET @tester1_pw = '7abcddbb2c74e4c0789c2c0aa6abcf5172e82e9f4916bc6409fc3989ed673e08';
SET @tester2_pw = '7cd477192d54ceb8673be093f443b8622c612896880f6879c7f8ec16fa7ba114';
SET @admin_pw = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';

INSERT IGNORE INTO account (username, password_hash, phone_number, address, email) VALUES
('tester1',      @tester1_pw, '7135550101', 'Houston, TX', 'tester1@gmail.com'),
('tester2',      @tester2_pw, '7135550102', 'Houston, TX', 'tester2@gamil.com'),
('marcus_r',     @pw, '7134450192', 'Houston, TX', 'marcus.r@gmail.com'),
('sofia_m',      @pw, '8328871045', 'Houston, TX', 'sofia.m@gmail.com'),
('jalen_w',      @pw, '7138329901', 'Houston, TX', 'jalen.w@gmail.com'),
('priya_k',      @pw, '8329004421', 'Houston, TX', 'priya.k@gmail.com'),
('daniela_g',    @pw, '7136671230', 'Houston, TX', 'daniela.g@gmail.com'),
('tyler_b',      @pw, '2813345678', 'Houston, TX', 'tyler.b@gmail.com'),
('aisha_t',      @pw, '7139887712', 'Houston, TX', 'aisha.t@gmail.com'),
('kevin_lh',     @pw, '8324419900', 'Houston, TX', 'kevin.lh@gmail.com'),
('natalie_v',    @pw, '7132209814', 'Houston, TX', 'natalie.v@gmail.com'),
('carlos_m',     @pw, '8321103456', 'Houston, TX', 'carlos.m@gmail.com'),
('jade_p',       @pw, '7134567890', 'Houston, TX', 'jade.p@gmail.com'),
('ryan_ok',      @pw, '2814321099', 'Houston, TX', 'ryan.ok@gmail.com'),
('isabella_r',   @pw, '7137890123', 'Houston, TX', 'isabella.r@outlook.com'),
('devin_c',      @pw, '8326541230', 'Houston, TX', 'devin.c@gmail.com'),
('yara_h',       @pw, '7139012345', 'Houston, TX', 'yara.h@gmail.com'),
('ethan_s',      @pw, '2812345678', 'Houston, TX', 'ethan.s@gmail.com'),
('mia_flores',   @pw, '7131234567', 'Houston, TX', 'mia.flores@yahoo.com'),
('brandon_k',    @pw, '8323456789', 'Houston, TX', 'brandon.k@gmail.com'),
('chloe_n',      @pw, '7135678901', 'Houston, TX', 'chloe.n@gmail.com'),
('darius_f',     @pw, '2816789012', 'Houston, TX', 'darius.f@gmail.com'),
('elena_w',      @pw, '7137890124', 'Houston, TX', 'elena.w@outlook.com'),
('oscar_t',      @pw, '8329012345', 'Houston, TX', 'oscar.t@gmail.com'),
('zoe_b',        @pw, '7130123456', 'Houston, TX', 'zoe.b@gmail.com'),
('alex_dm',      @pw, '2811234567', 'Houston, TX', 'alex.dm@gmail.com'),
('jasmine_r',    @pw, '7132345678', 'Houston, TX', 'jasmine.r@yahoo.com'),
('noah_c',       @pw, '8324567890', 'Houston, TX', 'noah.c@gmail.com'),
('camila_s',     @pw, '7136789012', 'Houston, TX', 'camila.s@gmail.com'),
('jordan_t',     @pw, '2817890123', 'Houston, TX', 'jordan.t@gmail.com'),
('aaliyah_b',    @pw, '7138901234', 'Houston, TX', 'aaliyah.b@gmail.com'),
('sean_p',       @pw, '8320123456', 'Houston, TX', 'sean.p@gmail.com'),
('valentina_c',  @pw, '7131234568', 'Houston, TX', 'valentina.c@outlook.com'),
('malik_j',      @pw, '2812345679', 'Houston, TX', 'malik.j@gmail.com'),
('hannah_l',     @pw, '7133456789', 'Houston, TX', 'hannah.l@gmail.com'),
('chris_ab',     @pw, '8325678901', 'Houston, TX', 'chris.ab@gmail.com'),
('destiny_m',    @pw, '7137890125', 'Houston, TX', 'destiny.m@yahoo.com'),
('luke_rj',      @pw, '2818901234', 'Houston, TX', 'luke.rj@gmail.com'),
('sarah_no',     @pw, '7139012346', 'Houston, TX', 'sarah.no@gmail.com'),
('tre_w',        @pw, '8321234567', 'Houston, TX', 'tre.w@gmail.com');

-- Admin demo accounts.
INSERT INTO account (username, password_hash, phone_number, address, email, is_admin, is_suspended)
VALUES
('admin',         @admin_pw, '5550000000', 'Houston, TX', 'admin@commonground.com', TRUE, FALSE),
('adriana_admin', @admin_pw, '5559999999', 'Houston, TX', 'adriana_admin@commonground.com', TRUE, FALSE)
ON DUPLICATE KEY UPDATE
password_hash = VALUES(password_hash),
email = VALUES(email),
is_admin = VALUES(is_admin),
is_suspended = VALUES(is_suspended);

-- Keep tester demo accounts correct if this script is run over older seed data.
UPDATE account
SET password_hash = @tester1_pw,
    email = 'tester1@gmail.com',
    is_admin = FALSE,
    is_suspended = FALSE
WHERE username = 'tester1';

UPDATE account
SET password_hash = @tester2_pw,
    email = 'tester2@gamil.com',
    is_admin = FALSE,
    is_suspended = FALSE
WHERE username = 'tester2';

-- client row for every account
INSERT IGNORE INTO client (account_id, can_list, can_purchase)
SELECT account_id, TRUE, TRUE FROM account
WHERE is_admin = FALSE
  AND (email LIKE '%@commonground.com' OR email LIKE '%@gmail.com' OR email LIKE '%@gamil.com' OR email LIKE '%@yahoo.com' OR email LIKE '%@outlook.com');

-- ─────────────────────────────────────────
--  LISTINGS  (distributed across accounts)
-- ─────────────────────────────────────────
SET @loc = 1;

-- ── MEN'S CLOTHING ───────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = "Men's Clothing");

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='marcus_r'),@loc,@cat,'Cash',"Levi's 501 Original Fit Jeans – 32x30",35.00,"Classic dark wash straight-leg Levi's 501s. Worn maybe 10 times — no fading, rips, or stains. Great everyday jean that goes with everything.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1542272604-787c3835535d?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tyler_b'),@loc,@cat,'Cash','Nike Tech Fleece Full-Zip Hoodie – Size L',65.00,"Men's Nike Tech Fleece in heather grey, size Large. Barely worn — still has that soft brushed feel inside. No pilling or damage.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jalen_w'),@loc,@cat,'Cash','Vintage Levi Denim Trucker Jacket – Size M',50.00,"90s Levi's trucker denim jacket, size Medium. Faded wash with natural wear that gives it character. All buttons intact.",'Fair','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='sean_p'),@loc,@cat,'Cash','Ralph Lauren Polo Shirt Bundle – Size M (4 shirts)',48.00,"Four classic Ralph Lauren polo shirts in size Medium — navy, white, green, and red. All in excellent condition, no fading or pilling.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1625910513586-09cb7e1efcd9?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='chris_ab'),@loc,@cat,'Cash','Carhartt WIP Beanie + Scarf Set – Black',22.00,"Carhartt WIP knit beanie and matching scarf in classic black. Worn one winter season. No pilling, acrylic knit still looks sharp.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1520903920243-00d872a2d1c9?w=800');

-- ── WOMEN'S CLOTHING ─────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = "Women's Clothing");

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='sofia_m'),@loc,@cat,'Cash','Zara Satin Wrap Midi Dress – Size S',38.00,"Dusty rose satin wrap dress from Zara, size Small. Worn once to a birthday dinner. Flows beautifully, hits mid-calf.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='natalie_v'),@loc,@cat,'Cash','H&M Oversized Linen Blazer – Size M',42.00,"Cream linen blazer from H&M, size Medium. Perfect for casual office looks or brunch. Light and breathable. Worn twice, no damage.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jade_p'),@loc,@cat,'Cash','SKIMS Cotton Ribbed Tank – Size S',18.00,"SKIMS cotton ribbed tank in sand/beige, size Small. Soft, stretchy, and flattering. Worn a handful of times, washed on gentle. No damage.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1485968579580-b6d095142e6e?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='camila_s'),@loc,@cat,'Cash','Free People Boho Maxi Skirt – Size XS/S',30.00,"Flowy Free People maxi skirt in a sage green floral print. Elastic waist, super comfortable. Perfect for summer. Light wash wear, no damage.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1583496661160-fb5218afa404?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='aaliyah_b'),@loc,@cat,'Cash','Aritzia Wilfred Cocoon Coat – Size XS',120.00,"Aritzia Wilfred Cocoon coat in camel, size XS. Classic silhouette, super warm. Dry cleaned and stored well. Minor wear on cuffs. Retail $298.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=800');

-- ── KIDS' CLOTHING ───────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = "Kids' Clothing");

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='isabella_r'),@loc,@cat,'Cash','Baby Gap Onesie Bundle – 6-9M (7 pieces)',20.00,"Seven Baby Gap onesies in size 6-9 months. Mix of short and long sleeve. Gently used, washed on gentle. My baby outgrew them too fast!",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='mia_flores'),@loc,@cat,'Cash','Nike Kids Tracksuit Set – Size 6',28.00,"Nike matching tracksuit jacket and pants for kids size 6. Navy blue with white stripes. Worn a couple times, excellent condition.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=800');

-- ── SHOES & FOOTWEAR ─────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Shoes & Footwear');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jalen_w'),@loc,@cat,'Cash','Nike Air Max 270 – Men Size 11',85.00,"Nike Air Max 270 in black/white, men's size 11. Worn about 15 times. Soles in great shape, no separation. Cleaned and ready.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='priya_k'),@loc,@cat,'Cash','Dr. Martens 1460 Boots – Women Size 8',95.00,"Classic Dr. Martens 1460 boots in cherry red, women's size 8. Worn one season. Broken in and super comfortable. No heel damage.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='yara_h'),@loc,@cat,'Cash','Adidas Ultraboost 22 – Women Size 7',70.00,"Adidas Ultraboost 22 running shoes, white colorway, women's size 7. Barely worn — maybe 3 runs. Comes with original box.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='darius_f'),@loc,@cat,'Cash','New Balance 550 – Men Size 10 (White/Green)',88.00,"New Balance 550 retro basketball shoe in white and green. Men's size 10. Deadstock, worn twice. Comes with box and extra laces.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1539185441755-769473a23570?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tre_w'),@loc,@cat,'Cash','Timberland 6" Premium Boots – Men Size 10',75.00,"Classic wheat Timberland 6-inch premium waterproof boots, men's size 10. Worn for about one season. Good tread remaining, leather is conditioned.",'Fair','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1520639888713-7851133b1ed0?w=800');

-- ── BAGS & ACCESSORIES ───────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Bags & Accessories');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='sofia_m'),@loc,@cat,'Cash','Coach Tabby Shoulder Bag – Tan Leather',165.00,"Authentic Coach Tabby shoulder bag in tan pebbled leather. Used 6 months. Minor wear on corners, interior clean. Comes with dust bag.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='ethan_s'),@loc,@cat,'Cash','Herschel Little America Backpack – Black',40.00,"Herschel Little America 25L backpack in classic black. Used for one semester. No tears, zippers perfect. Fits 15-inch laptop.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='destiny_m'),@loc,@cat,'Cash','Louis Vuitton Neverfull MM Tote – Damier Ebene',680.00,"Authentic LV Neverfull MM in Damier Ebene. Purchased at LV Houston Galleria. Light patina on handles, interior is clean. Comes with pouch and dustbag.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800');

-- ── JEWELRY & WATCHES ────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Jewelry & Watches');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='marcus_r'),@loc,@cat,'Cash','Fossil Gen 5 Smartwatch – Silver/Brown Leather',110.00,"Fossil Gen 5 smartwatch with silicone and leather bands included. Wear OS, works with Android or iPhone. Battery holds all day. Light scratches on case.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='valentina_c'),@loc,@cat,'Cash','Gold Layered Paperclip Chain Necklace Set (3pc)',18.00,"Set of 3 gold-plated layered necklaces — paperclip, box chain, and figaro. Lengths 16, 18, 20 inches. No tarnish, all clasps work.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='chloe_n'),@loc,@cat,'Cash','Pandora Moments Charm Bracelet + 5 Charms',85.00,"Pandora sterling silver moments bracelet with 5 charms: star, heart, flower, initial C, and birthstone. Cleaned and polished. Comes in Pandora box.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1611591437281-460bfbe1220a?w=800');

-- ── ELECTRONICS ──────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Electronics');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='kevin_lh'),@loc,@cat,'Cash','Sony 55" 4K OLED Smart TV (XBR-55A8H)',480.00,"Sony Bravia 55-inch 4K OLED. Picture quality is stunning — perfect blacks. Moving and can't take it. Remote, stand, all cords included. No dead pixels.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1593784991095-a205069470b6?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='brandon_k'),@loc,@cat,'Cash','Bose QuietComfort 45 Headphones – Black',155.00,"Bose QC45 wireless noise-canceling headphones. Best-in-class ANC. Battery life still 20+ hours. Comes with case, charging cable, and aux cord.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='oscar_t'),@loc,@cat,'Cash','JBL Charge 5 Portable Bluetooth Speaker – Teal',75.00,"JBL Charge 5. Waterproof, dustproof, charges your phone. Bass hits hard. Used at a few trips. Scratches on rubber housing, works perfectly.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='luke_rj'),@loc,@cat,'Cash','Sony WH-1000XM5 Wireless Headphones – Black',195.00,"Sony WH-1000XM5, industry-leading noise canceling. 30-hour battery, multipoint connection. Barely used, no scratches. Comes with case and cables.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='hannah_l'),@loc,@cat,'Cash','Amazon Echo Show 8 (2nd Gen) – Charcoal',55.00,"Amazon Echo Show 8 smart display with Alexa. 8-inch HD screen, great for video calls, recipes, smart home control. Works perfectly, minor scuff on back.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1518444065439-e933c06ce9cd?w=800');

-- ── PHONES & TABLETS ─────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Phones & Tablets');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='aisha_t'),@loc,@cat,'Cash','iPhone 13 Pro – 256GB Sierra Blue (Unlocked)',560.00,"iPhone 13 Pro 256GB in Sierra Blue, factory unlocked. Always had a case and screen protector. Battery health 89%. No cracks, no repairs. Original box.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='devin_c'),@loc,@cat,'Cash','Samsung Galaxy Tab S8 – 128GB WiFi (Graphite)',380.00,"Samsung Galaxy Tab S8 with S Pen. Light scratch on back, screen is perfect. Great for school or drawing. Charger and case included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='ryan_ok'),@loc,@cat,'Cash','Samsung Galaxy S22 – 128GB Phantom White',320.00,"Samsung Galaxy S22 128GB unlocked. Minor scuff on frame, screen flawless. Battery health strong. Original box and USB-C cable included.",'Fair','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1605236453806-6ff36851218e?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='alex_dm'),@loc,@cat,'Cash','Google Pixel 7 Pro – 128GB Snow (Unlocked)',410.00,"Google Pixel 7 Pro in Snow, 128GB, factory unlocked. Camera is incredible. Screen has tiny hairline scratch (barely visible). Battery health 91%. Charger included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1598327106026-d9521da673d1?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jasmine_r'),@loc,@cat,'Cash','iPad Air 5th Gen – 64GB WiFi Space Gray',390.00,"iPad Air 5th generation, M1 chip, 64GB WiFi in Space Gray. With Apple Pencil 2nd gen and Smart Folio case. Light wear on case corners, device is pristine.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800');

-- ── COMPUTERS & LAPTOPS ──────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Computers & Laptops');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tester1'),@loc,@cat,'Cash','MacBook Air M1 – 8GB/256GB Space Gray',760.00,"Apple MacBook Air M1, 8GB RAM, 256GB SSD. Battery cycle count 148. Light surface scratches on lid. Charger included. Runs macOS Sonoma.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='noah_c'),@loc,@cat,'Cash','Dell XPS 15 – i7/16GB/512GB + GTX 1650 Ti',680.00,"Dell XPS 15, Intel Core i7-10750H, 16GB RAM, 512GB NVMe, GTX 1650 Ti. 15.6-inch 4K OLED touch screen. Minor keyboard wear. Charger included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='malik_j'),@loc,@cat,'Cash','Custom Gaming PC – RTX 3070 / i7-12700K / 32GB',980.00,"Full gaming PC build: RTX 3070, Intel i7-12700K, 32GB DDR5, 1TB NVMe SSD, 750W PSU, mid-tower case with RGB. Runs everything at max settings.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1587202372634-32705e3bf49c?w=800');

-- ── GAMING ───────────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Gaming');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='darius_f'),@loc,@cat,'Cash','PlayStation 5 Digital Edition – Console Only',390.00,"PS5 Digital Edition. Works perfectly, never had issues. Smoke-free home. Includes all original cables and stand.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tester2'),@loc,@cat,'Cash','Nintendo Switch OLED – White + 4 Games',310.00,"Nintendo Switch OLED with Mario Kart 8, Animal Crossing, Zelda BOTW, and Splatoon 3. Screen no scratches. Joy-cons work perfectly. Dock included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jordan_t'),@loc,@cat,'Cash','Xbox Series X – Console + 2 Controllers',480.00,"Xbox Series X console with 2 wireless controllers (one carbon black, one robot white). All cables included. Plays 4K at 120fps. Works flawlessly.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1605901309584-818e25960a8f?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='brandon_k'),@loc,@cat,'Cash','Xbox Series X Controller – Carbon Black',38.00,"Official Xbox Series X controller. Used a dozen sessions, thumbstick grip still perfect. No drift. USB-C port works fine.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1593118247619-e2d6f056869e?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='oscar_t'),@loc,@cat,'Cash','Steam Deck 512GB + Carrying Case',420.00,"Valve Steam Deck 512GB NVMe. Plays AAA games on the go. Screen protector applied since day one. 8 months old. Comes with official case and charger.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1649944049991-698c2e9b7fcb?w=800');

-- ── CAMERAS & PHOTOGRAPHY ────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Cameras & Photography');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='elena_w'),@loc,@cat,'Cash','Canon EOS Rebel SL3 + 18-55mm Kit Lens',520.00,"Canon EOS Rebel SL3 DSLR, shutter count under 3,000. 4K video, 24MP stills. Comes with 2 batteries, charger, 32GB SD card, and original box.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='carlos_m'),@loc,@cat,'Cash','GoPro Hero 10 Black + Accessories Bundle',220.00,"GoPro Hero 10. 5.3K video, incredible stabilization. Comes with 2 batteries, dual charger, chest mount, and head strap.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='sarah_no'),@loc,@cat,'Cash','Sony ZV-1 Vlog Camera – Black',280.00,"Sony ZV-1 compact vlog camera. Perfect for YouTube and content creation. 20.1MP, 4K video, built-in ND filter, directional mic. Barely used. Box included.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1617042375876-a13e36732a04?w=800');

-- ── FURNITURE ────────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Furniture');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='zoe_b'),@loc,@cat,'Cash','IKEA FRIHETEN Sleeper Sectional – Dark Gray',350.00,"IKEA Friheten corner sofa-bed in dark gray. Converts to a full-size bed with storage. 2 years old. Minor armrest fading, cushions still firm. Buyer must bring help.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tester1'),@loc,@cat,'Cash','Mid-Century Modern Writing Desk – Walnut Finish',195.00,"Solid wood mid-century desk in walnut finish, 48x24 inches. Two small drawers. Light surface scratches on top but sturdy and beautiful.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='chris_ab'),@loc,@cat,'Cash','Ergonomic Mesh Office Chair – Black',88.00,"High-back ergonomic mesh office chair. Lumbar support, adjustable armrests, seat height, tilt tension. Used 1 year. Mesh clean, wheels roll smoothly.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='luke_rj'),@loc,@cat,'Cash','Queen Size Bed Frame – Dark Walnut (No Mattress)',120.00,"Solid wood queen platform bed frame in dark walnut. Slat support included. No box spring needed. Minor scuff on one leg. Disassembles easily.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800');

-- ── APPLIANCES ───────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Appliances');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='priya_k'),@loc,@cat,'Cash','Instant Pot Duo 8-Quart 7-in-1 Pressure Cooker',58.00,"Instant Pot Duo 8qt. All functions work. Fully cleaned, sealing ring included. Upgraded to a larger model. Original box included.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='camila_s'),@loc,@cat,'Cash','Ninja AF101 Air Fryer – 4 Quart',45.00,"Ninja Air Fryer 4QT. Makes perfectly crispy food with little oil. Used maybe 20 times, always cleaned well. Compact and fits any counter.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1574269909862-7e1d70bb8078?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='hannah_l'),@loc,@cat,'Cash','Dyson V8 Cordless Vacuum – Absolute',165.00,"Dyson V8 Absolute cordless vacuum. Up to 40 min runtime. Comes with all original attachments. Filter cleaned. Suction is still strong. Upgraded to V15.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800');

-- ── KITCHEN & DINING ─────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Kitchen & Dining');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='natalie_v'),@loc,@cat,'Cash','KitchenAid Artisan Stand Mixer – 5qt Empire Red',195.00,"KitchenAid Artisan 5-quart stand mixer in Empire Red. Comes with flat beater, dough hook, and wire whip. Used once. Works perfectly.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1556909172-54557c7e4fb7?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tester2'),@loc,@cat,'Cash','Cuisinart 12-Piece Stainless Steel Cookware Set',80.00,"Cuisinart MultiClad Pro 12-piece set. Saucepans, sauté pan, stockpot, skillets, all with lids. Oven safe to 550°F. Some surface marks, no warping.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jordan_t'),@loc,@cat,'Cash','Nespresso Vertuo Pop Coffee Maker – White',55.00,"Nespresso Vertuo Pop. Makes coffee, espresso, gran lungo, and alto. Works perfectly, descaled twice. Just switched to drip coffee.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800');

-- ── TOOLS & HARDWARE ─────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Tools & Hardware');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tre_w'),@loc,@cat,'Cash','DeWalt 20V MAX Cordless Drill/Driver Kit',95.00,"DeWalt DCD771C2 20V drill kit. Includes drill, 2 batteries, fast charger, and kit bag. Used for some home projects. All bits included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1504148455328-c376907d081c?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='ryan_ok'),@loc,@cat,'Cash','Stanley FatMax 25-Piece Tool Set in Bag',42.00,"Stanley FatMax 25-piece set. Hammer, screwdrivers, pliers, tape measure, wrench set, utility knife, and level. All tools present and working.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1530124566582-a618bc2615dc?w=800');

-- ── BOOKS & MAGAZINES ────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Books & Magazines');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='isabella_r'),@loc,@cat,'Cash','Harry Potter Complete Hardcover Box Set (1-7)',65.00,"Entire Harry Potter series in hardcover. Minor cover rubs but all spines intact, no loose pages. No highlighting or writing inside.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='ethan_s'),@loc,@cat,'Cash','MCAT Complete 7-Book Prep Bundle – Kaplan',40.00,"Kaplan MCAT 7-book review set. Covers all subjects. Minimal highlighting in 2 books. Great for self-study.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='chloe_n'),@loc,@cat,'Cash','Atomic Habits – James Clear (Hardcover)',8.00,"Atomic Habits by James Clear, hardcover. Read once, no damage, no markings. One of the best productivity books. Passing it on.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='alex_dm'),@loc,@cat,'Cash','UH Engineering Textbook Bundle – 4 Books',55.00,"Four upper-division engineering textbooks used at UH: Thermodynamics (Cengel), Fluid Mechanics, Circuits (Hayt), and Statics (Hibbeler). Light highlighting throughout.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800');

-- ── MUSIC & INSTRUMENTS ──────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Music & Instruments');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='marcus_r'),@loc,@cat,'Cash','Fender CD-60S Acoustic Guitar – Natural + Bag',175.00,"Fender CD-60S dreadnought acoustic. Solid spruce top, plays beautifully. Comes with gig bag, extra strings, tuner, and capo. Light pick marks near soundhole.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jasmine_r'),@loc,@cat,'Cash','Casio CT-S300 61-Key Electronic Keyboard',55.00,"Casio CT-S300, 400 built-in tones, 77 rhythms. Perfect for beginners. Works on batteries or AC adapter (both included). All keys work.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=800');

-- ── MOVIES & TV ──────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Movies & TV');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='kevin_lh'),@loc,@cat,'Cash','Marvel MCU 23-Film Blu-ray Collection (Phases 1-3)',75.00,"Complete MCU Phases 1-3 Blu-ray — all 23 movies from Iron Man to Endgame. All discs play perfectly. Cases in great condition.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1585647347483-22b66260dfff?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tyler_b'),@loc,@cat,'Cash','Breaking Bad Complete Series DVD Box Set',25.00,"Breaking Bad all 5 seasons on DVD. Good condition, minor shelf wear on box. All 21 discs play without issue.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=800');

-- ── SPORTS & OUTDOORS ────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Sports & Outdoors');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jalen_w'),@loc,@cat,'Cash','Trek FX 3 Disc Hybrid Bike – Size M (2021)',580.00,"Trek FX 3 Disc flat bar road bike, medium frame, 2021. Hydraulic disc brakes, carbon fork. New tires 3 months ago. Some minor frame scratches.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='devin_c'),@loc,@cat,'Cash','Bowflex SelectTech 552 Adjustable Dumbbells (Pair)',195.00,"Bowflex SelectTech 552, 5-52.5 lbs each. Both dials click perfectly. Storage trays included. Replaced a whole rack of dumbbells.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1517963879433-6ad2b056d712?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='yara_h'),@loc,@cat,'Cash','Manduka PRO Yoga Mat – 71" Black',68.00,"Manduka PRO yoga mat, 71 inches, 6mm thick. Closed-cell surface, never absorbs sweat. Used 8 months, excellent condition. Comes with strap.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='sean_p'),@loc,@cat,'Cash','Callaway Strata Golf Club Set – 12 Piece + Bag',180.00,"Callaway Strata 12-piece golf set for men. Driver, fairway woods, irons, putter, and stand bag. Used for two seasons at Wildcat Golf Club. Great starter set.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1535131749006-b7f58c99034b?w=800');

-- ── TOYS & GAMES ─────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Toys & Games');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='mia_flores'),@loc,@cat,'Cash','Catan Base Game + Seafarers Expansion',38.00,"Catan base game + Seafarers expansion. Both complete — all pieces, cards, and boards. Tiles and cards in great shape.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1611996575749-79a3a250f948?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='noah_c'),@loc,@cat,'Cash','LEGO Technic Bugatti Chiron (42083) – Complete',185.00,"LEGO Technic Bugatti Chiron, 3,599 pieces. Fully built then disassembled and bagged by color. All pieces verified twice. All booklets included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='aaliyah_b'),@loc,@cat,'Cash','Jenga Giant Premium Hardwood Game',35.00,"Giant Jenga, hardwood blocks. Great for parties, BBQs, or game nights. Blocks are solid, no splinters. Storage bag included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800');

-- ── ART & COLLECTIBLES ───────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Art & Collectibles');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='elena_w'),@loc,@cat,'Cash','Original Acrylic Painting – Abstract Landscape 24x18"',90.00,"Original acrylic painting on canvas, 24x18 inches. Abstract landscape with blues, teals, and golds. Hand-painted, one of a kind. Ready to hang.",'New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='carlos_m'),@loc,@cat,'Cash','Funko Pop Marvel Collection – 12 Figures',55.00,"12 Marvel Funko Pops including Iron Man, Spider-Man, Thor, Black Panther, and more. All in boxes. Great collection starter.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1566577739112-5180d4bf9390?w=800');

-- ── BABY & KIDS ──────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Baby & Kids');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='isabella_r'),@loc,@cat,'Cash','UPPAbaby Vista V2 Stroller – Greyson Gray',420.00,"UPPAbaby Vista V2 with main seat, bassinet, and toddler seat. Cleaned thoroughly. No tears in fabric, frame rolls perfectly. Retail $1,000+.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1590736704728-f4730bb30770?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='mia_flores'),@loc,@cat,'Cash','IKEA SUNDVIK Convertible Crib – White',110.00,"IKEA Sundvik crib, converts to toddler bed. All hardware included. No recalls. Light scuffs on one rail. Mattress not included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=800');

-- ── BEAUTY & PERSONAL CARE ───────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Beauty & Personal Care');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='jade_p'),@loc,@cat,'Cash','Dyson Airwrap Multi-Styler – Complete Long Set',310.00,"Dyson Airwrap Complete with all 6 attachments for long hair. Works perfectly, upgraded to newer version. Minor wear on barrel ends. Retail $600.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='valentina_c'),@loc,@cat,'Cash','Urban Decay Naked3 Eyeshadow Palette',22.00,"Urban Decay Naked3, 12 rose-hued shades. Lightly used, no cracked shadows. Pans mostly full. Mirror intact.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='destiny_m'),@loc,@cat,'Cash','Charlotte Tilbury Pillow Talk Lip Bundle',45.00,"Charlotte Tilbury Pillow Talk lip liner, lipstick, and lip gloss bundle. All used twice. The lip liner is barely touched. Iconic shade, great gift.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1586495777744-4e6232bf4fba?w=800');

-- ── HEALTH & WELLNESS ────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Health & Wellness');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='zoe_b'),@loc,@cat,'Cash','Vitamix E310 Explorian Blender – Black',165.00,"Vitamix E310 64-oz blender. Makes the smoothest smoothies and soups. Container and blade clean. Comes with tamper. Retail $350.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='ethan_s'),@loc,@cat,'Cash','Theragun Prime Percussive Therapy Device',135.00,"Theragun Prime massage gun. 5 speeds, Bluetooth app. Battery lasts 120+ minutes per charge. 4 attachments and charger included. Some grip wear.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800');

-- ── AUTOMOTIVE ───────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Automotive');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='malik_j'),@loc,@cat,'Cash','WeatherTech FloorLiners – Toyota Camry 2018-2022',48.00,"WeatherTech all-weather floor liners, front and rear, for Toyota Camry 2018-2022. Laser fit, used 2 years, washed and looking great.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='chris_ab'),@loc,@cat,'Cash','NOCO Boost Plus GB40 1000A Jump Starter',58.00,"NOCO Boost Plus GB40 jump starter. Works on gas up to 6L and diesel up to 3L. Charges phones, built-in flashlight. Used twice.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tre_w'),@loc,@cat,'Cash','Husky 3-Drawer Mechanics Tool Set – 270 Pieces',115.00,"Husky 270-piece mechanic tool set. SAE and metric sockets, ratchets, wrenches, bits. All in rolling case. No missing pieces. Great for home or garage.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1530124566582-a618bc2615dc?w=800');

-- ── PET SUPPLIES ─────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Pet Supplies');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='hannah_l'),@loc,@cat,'Cash','Frisco Orthopedic Dog Bed – Large (40x30")',48.00,"Frisco memory foam orthopedic dog bed, large, 40x30 inches. Removable washable cover. Clean, foam still supportive. No odor.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='oscar_t'),@loc,@cat,'Cash','PetFusion Ultimate Cat Tree – 33" Tall',68.00,"PetFusion cat tree 33 inches. Solid base, multiple perches. Sisal scratching posts intact. Light fur on surfaces. My cat ignored it.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1574158622682-e40e69881006?w=800');

-- ── HOME DECOR ───────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Home Decor');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='camila_s'),@loc,@cat,'Cash','West Elm Marble & Brass Table Lamp',62.00,"West Elm table lamp with white marble base and brass hardware. 12 inches tall. Light scuff on base bottom, shade pristine. Retail $129.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='natalie_v'),@loc,@cat,'Cash','Large Ceramic Arch Vase – Matte White',28.00,"Matte white ceramic arch vase, 12 inches tall. Minimal modern shape. Looks great with dried pampas grass. Never used.",'New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='zoe_b'),@loc,@cat,'Cash','Boho Macrame Wall Hanging – 24" Wide',32.00,"Hand-knotted macrame wall hanging in natural cotton, 24 inches wide x 36 inches long. Bohemian style, makes any wall look great. New, never hung.",'New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1615529179035-e760f6a2dcee?w=800');

-- ── OFFICE EQUIPMENT ─────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Office Equipment');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='noah_c'),@loc,@cat,'Cash','LG 27" 4K UHD IPS Monitor (27UK850-W)',195.00,"LG 27UK850 4K UHD IPS monitor. USB-C charges laptop at 60W. HDR10. Hairline scratch in corner, not visible during use. Stand included.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='brandon_k'),@loc,@cat,'Cash','HP LaserJet Pro M15w Wireless Printer',65.00,"HP LaserJet Pro M15w compact wireless laser printer. Works with WiFi and HP Smart app. Replaced with color printer. Nearly full toner.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='malik_j'),@loc,@cat,'Cash','Logitech MX Master 3S Mouse + MX Keys Combo',95.00,"Logitech MX Master 3S mouse and MX Keys full-size keyboard combo. Both work flawlessly, charge via USB-C. Minor wear on space bar. Great for productivity.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800');

-- ── OTHER ────────────────────────────────
SET @cat = (SELECT category_id FROM category WHERE category_name = 'Other');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tester2'),@loc,@cat,'Cash','Hydro Flask 40oz Wide Mouth Bottle – Pacific',22.00,"Hydro Flask 40oz in Pacific teal. Keeps cold 24 hours or hot 12. Straw lid and flex cap included. Minor scratch, no dents.",'Good','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='tester1'),@loc,@cat,'Cash','YETI Rambler 30oz Tumbler – Navy',28.00,"YETI Rambler 30oz in navy. MagSlider lid included. Keeps coffee hot for hours. No dents, no rust. Lightly used.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1590439471364-192aa70c0b53?w=800');

INSERT INTO listing (client_id,location_id,category_id,payment_type,item_name,price,description,item_condition,status) VALUES
((SELECT account_id FROM account WHERE username='alex_dm'),@loc,@cat,'Cash','Polaroid Now+ Instant Camera – Black',88.00,"Polaroid Now+ analog instant camera in black. Bluetooth app for creative filters. Comes with 2 packs of Polaroid film (16 shots). Barely used.",'Like New','Available');
INSERT INTO listing_image (listing_id,file_name,file_path) VALUES (LAST_INSERT_ID(),'listing-image','https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800');

-- ─────────────────────────────────────────
--  RANDOMIZE TIMESTAMPS (spread over past 90 days)
-- ─────────────────────────────────────────
UPDATE listing
SET created_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 86400 * 90) SECOND)
WHERE status = 'Available';

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- confirm
SELECT CONCAT('✓ Accounts created: ', (SELECT COUNT(*) FROM account WHERE email LIKE '%@commonground.com' OR email LIKE '%@gmail.com' OR email LIKE '%@yahoo.com' OR email LIKE '%@outlook.com')) AS summary
UNION ALL
SELECT CONCAT('✓ Listings seeded: ', COUNT(*)) FROM listing WHERE status = 'Available'
UNION ALL
SELECT CONCAT('✓ Images attached: ', COUNT(*)) FROM listing_image;
