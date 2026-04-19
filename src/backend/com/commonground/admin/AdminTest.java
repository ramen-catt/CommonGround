package com.commonground.admin;

import com.commonground.listing.ServiceResult;
import java.util.List;

public class AdminTest {

    private static final AdminService service = new AdminService();

    private static final int TEST_ADMIN_ID    = 2;
    private static final int TEST_USER_ID     = 3;
    private static final int NON_ADMIN_ID     = 1;
    private static final int TEST_LISTING_ID  = 999;
    private static final int TEST_FEEDBACK_ID = 999;

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {

        printLine("=", 52);
        System.out.println("  CommonGround — Admin Tests | Adriana");
        System.out.println("  COSC 4319 | WeJustTrynaGraduate (WJTG)");
        printLine("=", 52);
        System.out.println();

        test01_AdminCheckReturnsTrue();
        test02_NonAdminCheckReturnsFalse();
        test03_NonAdminCannotDoAdminActions();
        test04_AdminCanSuspendUser();
        test05_AdminCannotSuspendAnotherAdmin();
        test06_AdminCannotSuspendSelf();
        test07_AdminCanUnsuspendUser();
        test08_AdminCanRemoveListing();
        test09_NonAdminCannotRemoveListing();
        test10_AdminCanViewReports();
        test11_AdminCanRemoveReview();
        test12_AdminCanViewAllAccounts();
        test13_AdminCanViewTransactionLog();

        System.out.println();
        printLine("=", 52);
        System.out.printf("  RESULTS: %d passed, %d failed%n", passCount, failCount);
        printLine("=", 52);
        if (failCount == 0) {
            System.out.println("  ALL TESTS PASSED — Admin feature is ready!");
        } else {
            System.out.println("  SOME TESTS FAILED — see FAIL messages above.");
        }
        printLine("=", 52);
    }

    private static void test01_AdminCheckReturnsTrue() {
        System.out.println("TEST 01: Admin check returns true for real admin");

        AdminDAO dao = new AdminDAO();
        try {
            boolean result = dao.isAdmin(TEST_ADMIN_ID);
            if (result) {
                pass("TEST 01", "account_id=" + TEST_ADMIN_ID + " correctly identified as admin.");
            } else {
                fail("TEST 01", "account_id=" + TEST_ADMIN_ID + " should be admin but returned false.");
            }
        } catch (Exception e) {
            fail("TEST 01", "DB error: " + e.getMessage());
        }
    }

    private static void test02_NonAdminCheckReturnsFalse() {
        System.out.println("TEST 02: Admin check returns false for regular user");

        AdminDAO dao = new AdminDAO();
        try {
            boolean result = dao.isAdmin(NON_ADMIN_ID);
            if (!result) {
                pass("TEST 02", "account_id=" + NON_ADMIN_ID + " correctly identified as non-admin.");
            } else {
                fail("TEST 02", "account_id=" + NON_ADMIN_ID + " should NOT be admin but returned true.");
            }
        } catch (Exception e) {
            fail("TEST 02", "DB error: " + e.getMessage());
        }
    }

    private static void test03_NonAdminCannotDoAdminActions() {
        System.out.println("TEST 03: Non-admin cannot perform admin actions");

        ServiceResult result = service.suspendUser(NON_ADMIN_ID, TEST_USER_ID);

        if (!result.isSuccess() && result.getMessage().contains("Access denied")) {
            pass("TEST 03", "Non-admin correctly blocked. Msg: '" + result.getMessage() + "'");
        } else {
            fail("TEST 03", "Non-admin should be blocked from admin actions. Got: " + result.getMessage());
        }
    }

    private static void test04_AdminCanSuspendUser() {
        System.out.println("TEST 04: Admin can suspend a regular user");

        ServiceResult result = service.suspendUser(TEST_ADMIN_ID, TEST_USER_ID);

        if (result.isSuccess()) {
            pass("TEST 04", "User " + TEST_USER_ID + " suspended successfully. Msg: '" + result.getMessage() + "'");
        } else {
            fail("TEST 04", "Admin should be able to suspend user. Got: " + result.getMessage());
        }
    }

    private static void test05_AdminCannotSuspendAnotherAdmin() {
        System.out.println("TEST 05: Admin cannot suspend another admin");

        ServiceResult result = service.suspendUser(TEST_ADMIN_ID, TEST_ADMIN_ID);

        if (!result.isSuccess()) {
            pass("TEST 05", "Correctly blocked suspending an admin. Msg: '" + result.getMessage() + "'");
        } else {
            fail("TEST 05", "Should not be able to suspend an admin account.");
        }
    }

    private static void test06_AdminCannotSuspendSelf() {
        System.out.println("TEST 06: Admin cannot suspend themselves");

        ServiceResult result = service.suspendUser(TEST_ADMIN_ID, TEST_ADMIN_ID);

        if (!result.isSuccess()) {
            pass("TEST 06", "Correctly blocked self-suspension.");
        } else {
            fail("TEST 06", "Admin should not be able to suspend their own account.");
        }
    }

    private static void test07_AdminCanUnsuspendUser() {
        System.out.println("TEST 07: Admin can unsuspend a user");

        ServiceResult result = service.unsuspendUser(TEST_ADMIN_ID, TEST_USER_ID);

        if (result.isSuccess()) {
            pass("TEST 07", "User " + TEST_USER_ID + " reinstated successfully. Msg: '" + result.getMessage() + "'");
        } else {
            fail("TEST 07", "Admin should be able to unsuspend user. Got: " + result.getMessage());
        }
    }

    private static void test08_AdminCanRemoveListing() {
        System.out.println("TEST 08: Admin can remove any listing");

        ServiceResult result = service.removeListing(TEST_ADMIN_ID, TEST_LISTING_ID);

        if (result.isSuccess()) {
            pass("TEST 08", "Listing removed by admin. Msg: '" + result.getMessage() + "'");
        } else {
            fail("TEST 08", "Admin should be able to remove any listing. Got: " + result.getMessage());
        }
    }

    private static void test09_NonAdminCannotRemoveListing() {
        System.out.println("TEST 09: Non-admin cannot remove a listing");

        ServiceResult result = service.removeListing(NON_ADMIN_ID, TEST_LISTING_ID);

        if (!result.isSuccess() && result.getMessage().contains("Access denied")) {
            pass("TEST 09", "Non-admin correctly blocked from removing listing.");
        } else {
            fail("TEST 09", "Non-admin should be blocked. Got: " + result.getMessage());
        }
    }

    private static void test10_AdminCanViewReports() {
        System.out.println("TEST 10: Admin can view all fraud reports");

        List<String[]> reports = service.viewReports(TEST_ADMIN_ID);

        if (reports != null) {
            pass("TEST 10", "viewReports() returned " + reports.size() +
                    " report(s). (Needs feedback with rating_report=TRUE to show results.)");
        } else {
            fail("TEST 10", "viewReports() returned null.");
        }
    }

    private static void test11_AdminCanRemoveReview() {
        System.out.println("TEST 11: Admin can remove a review");

        ServiceResult result = service.removeReview(TEST_ADMIN_ID, TEST_FEEDBACK_ID);

        if (result.isSuccess()) {
            pass("TEST 11", "Review removed by admin. Msg: '" + result.getMessage() + "'");
        } else {
            fail("TEST 11", "Admin should be able to remove any review. Got: " + result.getMessage());
        }
    }

    private static void test12_AdminCanViewAllAccounts() {
        System.out.println("TEST 12: Admin can view all accounts");

        List<String[]> accounts = service.viewAllAccounts(TEST_ADMIN_ID);

        if (accounts != null && accounts.size() > 0) {
            pass("TEST 12", "viewAllAccounts() returned " + accounts.size() + " account(s).");
        } else {
            fail("TEST 12", "viewAllAccounts() returned null or empty.");
        }
    }

    private static void test13_AdminCanViewTransactionLog() {
        System.out.println("TEST 13: Admin can view transaction log");

        List<String[]> txns = service.viewTransactionLog(TEST_ADMIN_ID);

        if (txns != null) {
            pass("TEST 13", "viewTransactionLog() returned " + txns.size() +
                    " transaction(s). (Will be 0 until Kelly's transactions are in.)");
        } else {
            fail("TEST 13", "viewTransactionLog() returned null.");
        }
    }

    private static void pass(String name, String message) {
        System.out.println("   ✓ PASS — " + message);
        System.out.println();
        passCount++;
    }

    private static void fail(String name, String message) {
        System.out.println("   ✗ FAIL — " + message);
        System.out.println();
        failCount++;
    }

    private static void printLine(String ch, int count) {
        System.out.println(ch.repeat(count));
    }
}