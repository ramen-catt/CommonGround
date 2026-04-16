use CommonGround;

Insert IGNORE INTO message(
    conversation_id, sender_id, receiver_id, message_text
)VALUES
    (1, 1, 2, "Hi I am testing the messaging service"),
    (1, 1, 2, "Hello! This is a message from sender 1 to receiver 2"),
    (1, 2, 1, "Hi! This is a message from sender 2 to receiver 1"),
    (2, 1, 3, "Hey there! This is a message from sender 1 to receiver 3"),
    (3, 1, "Hello! This is a message from sender 3 to receiver 1");

