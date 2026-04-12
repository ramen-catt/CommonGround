public class MessagingService
{
    public static void main(String args[])
    {

    }

    private void Accept_Message(String msg)//function called by send button, msg is sent to database
    {
        System.out.println(msg);//placeholder code that represents message being sent to database
    }

    private void Accept_Read()//function that creates message based on if the message reciever wants to start a conversation with sender
    {
        System.out.println("Conversation started");
        
    }

    private void Accept_Delete()//function that deletes message 
    {
        System.out.println("Message deleted");
    }

        public int GenMsgId()
    {
        
    }
}