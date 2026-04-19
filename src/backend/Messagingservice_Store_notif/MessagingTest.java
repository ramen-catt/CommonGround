package Messagingservice_Store_notif;

import java.sql.SQLException;

//        >> Remove-Item out -Recurse -Force
//        >> New-Item -ItemType Directory -Name out
//        >> javac -cp ".;lib/*" -d out src\backend\Messagingservice_Store_notif\*.java
//        >> java -cp ".;lib/*;out" com.commonground.Messagingservice_Store_notif.MessagingTest

public class MessagingTest {
    public static void main(String args[]) throws SQLException {
        MessagingService messagingService = new MessagingService();
        messagingService.SendMessage();
        messagingService.Open_Conversation(1);
        messagingService.Delete(1, 3);
        messagingService.Open_Conversation(1);
    }
}