package Messagingservice_Store_notif;

public class MessagingTest {
    public static void main(String args[]) {
        MessagingService ms = new MessagingService();
        ms.SendMessage("Hello World");
        ms.Accept_Conversation();
        ms.Decline_Conversation();
        ms.Accept_Delete();
    }
}