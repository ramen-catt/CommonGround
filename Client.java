import java.io.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;

public class Client
{
    public static void main(String[] args) throws SQLException {
        //At the moment the main function will be used for testing and for connecting database
        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String password = "root"; 

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (conn != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while connecting to the database.");
            e.printStackTrace();
        }

    }
    // Move createAccount and generateID to Guest class when it's created
    public static void createAccount() throws SQLException{
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
        int userID = generateID();

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
        stmt.setString(6, user_email);

        stmt.executeUpdate();

        kb.close();
    }
    public static int generateID() throws SQLException{
        /* Function generateID 
            This function will generate a unique userID for each new account created.
            Usually this function will only be called when the user creates a new account.
        */
        int min = 100000000;
        int max = 999999999;
        Random rand = new Random();
        int userID = rand.nextInt((max-min)+1) + min; //This will generate a random 9 digit ID for each user
        
        //Also make sure that each generated ID is unique by comparing it to the database
        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String password = "root";
        
        Connection conn = DriverManager.getConnection(url, user, password);
        PreparedStatement pstmt = conn.prepareStatement("SELECT account_ID FROM account WHERE account_ID = ?");
        pstmt.setInt(1, userID);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            generateID(); //If the generated ID is already in the database, generate a new one
        }
            
        return userID; //Returns the unique userID 
    }
    public static void updateAccount(int account_ID, String email, String password, long phone_number, String username, String address) throws SQLException{
        /* Function updateAccount
        This function will be used to update an existing user's account information.
        */
        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String sql_password = "root";
        
        Connection conn = DriverManager.getConnection(url, user, sql_password);

        String select = "SELECT * FROM account WHERE account_ID = ?";
        String insert = "INSERT INTO account (account_ID, username, password_hash, phone_number, address, email) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement insert_stmt = conn.prepareStatement(insert);
        PreparedStatement select_stmt = conn.prepareStatement(select);
        // Pull original account information from the database and display
            // Insert code here
        // Ask user to input updated account information
        Scanner kb = new Scanner(System.in);
        
        System.out.println("Enter updated email:");
        String new_email = kb.nextLine();

        System.out.println("Enter updated password: ");
        String new_password = kb.nextLine();

        System.out.println("Enter updated phone number:");
        int new_phone_number = kb.nextInt();

        System.out.println("Enter updated username: ");
        String new_username = kb.nextLine();

        System.out.println("Enter updated address: ");
        String new_address = kb.nextLine();

        // Ask user to confirm changes
        System.out.println("Confirm changes? (Yes/No)");
        String answer = kb.nextLine();
        if (answer.equals("Yes")){
            boolean confirm = true;
        }
        else{
            boolean confirm = false;
        }
        /* 
        if (confirm){
            // Enter code to change database information to new information here
            email = new_email;
            password = new_password;
            phone_number = new_phone_number;
            username = new_username;
            address = new_address;

        }
        */
        kb.close();
    }
        public static void deleteAccount(int userID) throws SQLException{
        /* Function deleteAccount
        This function will be used to delete an account from the database.
        */
        String url = "jdbc:mysql://localhost:3306/CG";
        String user = "root";
        String password = "root";
        
        Connection conn = DriverManager.getConnection(url, user, password);

        String delete_stmt = "DELETE FROM account WHERE account_ID = ?";
        PreparedStatement stmt = conn.prepareStatement(delete_stmt);
        stmt.setInt(1, userID);
        stmt.executeUpdate();
    }
}