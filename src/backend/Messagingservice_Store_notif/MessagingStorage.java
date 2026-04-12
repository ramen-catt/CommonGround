class MessagingService
{
    public void CreateMsgRec(int msgId, int senderId, int recieverId, String msg, Time date)
    {
        this.msgId = msgId;
        this.senderId = senderId;
        this.RecieverId = recieverId;
        this.msg = msg;
        this.date = date;
    }

    public String[] GetConversation(int userId1, int userId2)
    {
        
    }

    public String GetMessage(int userId)
    {

    }

    public void DeleteMsgRec(int msgId)
    {
        
    }




}