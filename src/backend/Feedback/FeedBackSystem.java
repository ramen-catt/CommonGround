import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FeedBackSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<FeedBack> feedbacklist = new ArrayList<>();

    public static void main(String[] args){
        System.out.print("====FeedBack Collection System====");

        while (true) {
            System.out.println("\n1. Submit Feedback");
            System.out.println("2. View All Feedback");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    collectFeedback();
                    break;
                case "2":
                    displayFeedback();
                    break;
                case "3":
                    System.out.println("Exiting... Thank you!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again");
            }
        }
    }

    private static void collectFeedback(){
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()){
            System.out.println("Name cannot be empty");
            return;
        }

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")){
            System.out.println("Invalid email format.");
            return;
        }

        System.out.print("Enter your feedback message: ");
        String message  = scanner.nextLine().trim();
        if (message.isEmpty()){
            System.out.println("Feedback message cannot be empty");
            return;
        }

        System.out.print("Enter rating (1-5): ");
        int rating;
        try{
            rating = Integer.parseInt(scanner.nextLine().trim());
            if (rating < 1 || rating > 5){
                System.out.println("Rating must be between 1 and 5.");
                return;
            }
        } catch (NumberFormatException e){
            System.out.println("Invalid rating. Please enter a number between (1-5).");
            return;
        }

        feedbacklist.add(new FeedBack(name, email, message, rating));
        System.out.println("Thank you! Your feedback has been recorded.");

    }

    private static void displayFeedback() {
        if (feedbacklist.isEmpty()) {
            System.out.println("No feedback available.");
        } else {
            System.out.println("\n=== All Feedback ===");
            for (FeedBack fb : feedbacklist) {
                System.out.println(fb);
                System.out.println("-----------------");
            }
        }
    }
}
