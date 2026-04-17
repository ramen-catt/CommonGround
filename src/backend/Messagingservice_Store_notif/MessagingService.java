package Messagingservice_Store_notif;
import java.lang.*;
import java.sql.SQLException;
import java.util.Scanner;
class MessagingService
{
    public static void main(String args[]) throws SQLException
    {
        SendMessage();
    }

    public static void SendMessage() throws SQLException//function called by send button, msg is sent to database and recieving client
    {
        MessageRecord msgRec = new MessageRecord();
        MessagingDAO MessagingDAO = new MessagingDAO();
        Scanner sc = new Scanner(System.in);//placeholder code that represents message being sent to database
        System.out.println("message here");
        String msg = sc.nextLine();
        System.out.println(msg);// actually pring this in ui for BOTH users

        msgRec.MessageRec(1, //grabbed from UI convo probably :/
        1,//increments in database, database team can edit this to fit their needs
        1, 2, //grab sender and reciever id from client info
        msg);//creates message record to be sent to database, database team can edit this function to fit their needs
        
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
    private static void Delete()//function that deletes message from client view and deletes message record after a certain amount of time
    {
        System.out.println("Message deleted");
    }
/* 
    private void Open_Conversation(int conversationId)//grabs all messages based on convo id
    {
        while()
        {
            System.out.println("");
        }
    }
    */
}