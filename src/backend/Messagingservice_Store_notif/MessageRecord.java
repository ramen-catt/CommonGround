package Messagingservice_Store_notif;

public class MessageRecord {
    int convoId= 0;
    int msgId = 0;
    int senderId = 0;
    int receiverId = 0;
    String content = "";

    public void MessageRec(int convoId, int msgId, int senderId, int receiverId, String msg)
    {
        this.convoId = convoId;
        this.msgId = msgId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = msg;
    }

    // Getters
    public int getConvoId(){
        return convoId;
    }

    public int getMsgId() {
        return msgId;
    }

    public int getSender() {
        return senderId;
    }

    public int getReceiver() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setConvoId(int convoId){

    }
    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public void setSender(int senderId) {
        this.senderId = senderId;
    }

    public void setReceiver(int receiverId) {
        this.receiverId = receiverId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
