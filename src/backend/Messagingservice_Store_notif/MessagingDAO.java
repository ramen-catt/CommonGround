package Messagingservice_Store_notif;
//   to talk directly to MySQL. Everything else goes through here. Important for database team, this is a seperate storage 

//note to self add images to make sure they are entered through clients computer not to be stored in database 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MessagingDAO {

//random user I made to test, database people can edit here 
private static final String DB_URL  = "jdbc:mysql://localhost:3307/CommonGround_db";
private static final String DB_USER = "root";
private static final String DB_PASS = "root";

   //open new connection to test
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public void createMessageRecord(MessageRecord message) throws SQLException {

        String sql = "INSERT IGNORE INTO message (convoId, msgId, sender, receiver, content, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, message.getConvoId());
            stmt.setInt(2, message.getMsgId());
            stmt.setInt(3, message.getSender());
            stmt.setInt(4, message.getReceiver());
            stmt.setString(5, message.getContent());
            stmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));


            stmt.executeUpdate();
        }
        
    }
}
