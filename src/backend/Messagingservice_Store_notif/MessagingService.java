package Messagingservice_Store_notif;

import java.lang.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class MessagingService
{
    public static void main(String args[]) throws SQLException
    {
        
    }

    public static void SendMessage() throws SQLException//function called by send button, msg is sent to database and recieving client
    {
        MessageRecord msgRec = new MessageRecord();
        MessagingDAO MessagingDAO = new MessagingDAO();
        Scanner sc = new Scanner(System.in);//placeholder code that represents message being sent to database
        System.out.println("message here");
        String msg = sc.nextLine();
        System.out.println(msg);// actually pring this in ui for BOTH users

        msgRec.MessageRec(1, 1,1, 2,msg);
        
        MessagingDAO.createMessageRecord(msgRec);//sends smessage record to database, database team can edit this function to fit their needs

    }
/* 
    private static void AcceptOrDecline_Conversation(boolean accept)//function that creates conversation based on if the message reciever wants to start a conversation with sender
    {
        if(accept)
        {
            System.out.println("Conversation started");
            return;
        }
        System.out.println("Conversation declined");
        return;
    }
*/
    void Delete(int convoId, int msgId) throws SQLException//function that deletes message from client view and deletes message record after a certain amount of time
    {
        MessagingDAO MessagingDAO = new MessagingDAO();
        try {
            MessagingDAO.deleteMessageRecord(convoId, msgId);
        } catch (SQLException e) {
            System.out.println("Error deleting message: " + e.getMessage());
        }
    }

    static void Open_Conversation(int conversationId) throws SQLException//grabs all messages based on convo id
    {
        List<String[]> messages = new ArrayList<>();
        MessagingDAO MessagingDAO = new MessagingDAO();
        messages = MessagingDAO.getConversation(conversationId);
        for(String[] message : messages)
        {
            System.out.println("Message: " + message[1] + " Conversation ID: " + message[0]);
        }
    }
    
}