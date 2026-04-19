package Messagingservice_Store_notif;
//   to talk directly to MySQL. Everything else goes through here. Important for database team, this is a seperate storage 

//note to self add images to make sure they are entered through clients computer not to be stored in database 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

        String sql = "INSERT IGNORE INTO message (conversation_id, sender_id, receiver_id, message_text, sent_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        int conversationId = getOrCreateConversation(
        message.getSender(), 
        message.getReceiver()
    );

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            stmt.setInt(2, message.getSender());
            stmt.setInt(3, message.getReceiver());
            stmt.setString(4, message.getContent());


            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
        
    }

    public List<String[]> getConversation(int convoId) throws SQLException 
    {

        String sql = "SELECT conversation_id, message_text FROM message WHERE conversation_id = ? ORDER BY sent_at";
        List<String[]> messages = new ArrayList<>();

        try (Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) 
            {

            stmt.setInt(1, convoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) 
                {
                    messages.add(new String[]
                    {
                        String.valueOf(rs.getInt("conversation_id")),
                        rs.getString("message_text")
                    });
                }
            }
        return messages;
    }

    public void deleteMessageRecord(int convoId, int msgId) throws SQLException {

        String sql = "DELETE FROM message WHERE conversation_id = ? AND message_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, convoId);
            stmt.setInt(2, msgId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
        
    }
    /*
        ========================================================================================
        TALK ABOUT THIS WITH GROUP, MAY NEED TO IGNORE BASED ON WEBSITE STUFF OR TIME RESTRAINTS
        ========================================================================================
    */
    public int getOrCreateConversation(int userId1, int userId2) throws SQLException 
    {

        // Check if conversation already exists between these two users
        String selectSql = """
            SELECT conversation_id FROM conversation
            WHERE (participant_1 = ? AND participant_2 = ?)
            OR (participant_1 = ? AND participant_2 = ?)
            LIMIT 1
        """;

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(selectSql)) {

            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("conversation_id"); // already exists, reuse it
            }
        }

        // No conversation found, create a new one
        String insertSql = "INSERT INTO conversation (participant_1, participant_2) VALUES (?, ?)";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1); // return the new conversation_id
            }
        }

        throw new SQLException("Failed to create conversation.");
    }

}
