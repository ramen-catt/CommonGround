package Messagingservice_Store_notif;

import java.util.Scanner;

class MessagingService {
    public static void main(String args[]) {

    }

    private void SendMessage(String msg)// function called by send button, msg is sent to database and recieving client
    {
        Scanner sc = new Scanner(System.in);// placeholder code to test but when i connect to ui this will be replaced
                                            // by text field input
        System.out.println("Enter message to send: ");
        String message = sc.nextLine();
        System.out.println("Message sent: " + message);

        messagingDAO.createMessageRecord(message);
    }

    private void AcceptOrDecline_Conversation(boolean accept)// function that creates conversation based on if the
                                                             // message reciever wants to start a conversation with
                                                             // sender
    {
        if (accept)// if true converstation is started and messages are sent to database and
                   // recieving client
        {
            System.out.println("Conversation started");
            return;
        }
        System.out.println("Conversation declined");
    }

    private void Delete()// function that deletes message from client view and deletes message record
                         // after a certain amount of time
    {
        System.out.println("Message deleted");
    }

    private void Open_Conversation(int conversationId)// retrieve all messages and displays on ui based on conversation Id
    {
        while()
        {
            System.out.println();
        }
    }
}