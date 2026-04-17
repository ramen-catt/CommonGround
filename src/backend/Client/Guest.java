import java.io.*;
import java.sql.*;
import java.util.*;


public class Guest 
{
    public static void main (String[] args) throws SQLException{
        //This main function will be used for testing the Guest functions
        createAccount();
     
    }
    public static void Login(String username, String password) throws SQLException{
        /*Function Login
        This function will be used for guest users to log in to the system.
        */
        Scanner kb = new Scanner(System.in);

        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String sql_password = "root";
        
        Connection conn = DriverManager.getConnection(url, user, sql_password);
        //Check database for username and password connection
        PreparedStatement pstmt = conn.prepareStatement("SELECT account_ID, username, password_hash FROM account WHERE username = ? AND password_hash = ?");

        pstmt.setString(1, username);
        pstmt.setString(2, password);
    }
    public static int createAccount() throws SQLException{
        /* Function createAccount
        This function will typically be used one per unique user. It's used to create
        a new account with the typical inputs of email, password, phone number, and address.
        */
        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String password = "root";
        
        Connection conn = DriverManager.getConnection(url, user, password);

        String sql = "INSERT INTO account (account_ID, username, password_hash, phone_number, address, email) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        Scanner kb = new Scanner(System.in);

        // Testing Fields for createAccount function
        int userID = Client.generateID();

        System.out.println("Please enter your email: ");
        String user_email = kb.nextLine();

        System.out.println("Please enter your password: ");
        String user_password = kb.nextLine();

        System.out.println("Please enter your phone number: ");
        String user_phone_num = kb.nextLine();
        long user_phone_number = Long.parseLong(user_phone_num);

        System.out.println("Please enter your username: ");
        String user_username = kb.nextLine();

        System.out.println("Please enter your address: ");                
        String user_address = kb.nextLine();

        stmt.setInt(1, userID);
        stmt.setString(2, user_username);
        stmt.setString(3, user_password);
        stmt.setLong(4, user_phone_number);
        stmt.setString(5, user_address);
        checkEmail(user_email);
        stmt.setString(6, user_email);

        int active_user = userID;

        stmt.executeUpdate();

        kb.close();

        return active_user;
    }
    public static void checkEmail(String email) throws SQLException{
        /* Function checkEmail
        This function will be used to check if an email is already in use.
        */ 
        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String password = "root";
        
        Connection conn = DriverManager.getConnection(url, user, password);
        String sql = "SELECT email FROM account WHERE email = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.println("Email is already in use.");
        } else {
            pstmt.setString(1, email);
        }    
    }
}
