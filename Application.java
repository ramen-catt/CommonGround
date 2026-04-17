import java.sql.*;
import java.util.*;

public class Application {
    public static void main(String[] args) throws SQLException {
        /* Main function
            This is the main function that will run when the application is started.
            It will be used to test all the Client functions and the Guest functions.
        */
       //Guest.createAccount();
       Client.deleteAccount(799285901);
    }
}
