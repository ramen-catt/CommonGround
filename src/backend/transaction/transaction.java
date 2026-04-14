package transactions;

import database.AuditTrail;
import database.TransactionDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Transaction class for CommonGround.
 *
 * Handles the full lifecycle of a simulated peer-to-peer transaction between
 * a buyer and seller. No real money is transferred — CommonGround only facilitates
 * the agreement and confirmation process. Users handle payments externally
 * (cash, Zelle, CashApp, Venmo, PayPal, etc.).
 *
 * Multithreaded design:
 * Both buyer and seller must call confirmTransaction() to proceed.
 * A CountDownLatch(2) blocks until both parties have confirmed, then
 * the system moves both users to the rating/review screen simultaneously.
 *
 * Based on class diagram:
 *   Generate_ID()
 *   Authorization(paymentInfo)
 *   Confirm_Payment_Type(bxnID)
 *   Status(bxnID)
 *   Check_Sold_Status(listID)
 *   Auth_Payment_Method(paymentInfo)
 *   Release_Payment(bxnID)
 */
public class Transaction {

    // ─── Fields ──────────────────────────────────────────────────────────────

    private String bxnID;           // Transaction ID (bxn = abbreviation used in diagram)
    private String listingID;       // The listing this transaction is for
    private String buyerUserID;
    private String sellerUserID;
    private PaymentInfo paymentInfo;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Multithreaded confirmation: latch starts at 2 (buyer + seller)
    private final CountDownLatch confirmationLatch;
    private final AtomicBoolean buyerConfirmed;
    private final AtomicBoolean sellerConfirmed;
    private final Object confirmLock = new Object();

    // Timeout for waiting on the other party (5 minutes)
    private static final long CONFIRM_TIMEOUT_MINUTES = 5;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public Transaction(String listingID, String buyerUserID, String sellerUserID, PaymentInfo paymentInfo) {
        this.bxnID = Generate_ID();
        this.listingID = listingID;
        this.buyerUserID = buyerUserID;
        this.sellerUserID = sellerUserID;
        this.paymentInfo = paymentInfo;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.confirmationLatch = new CountDownLatch(2);
        this.buyerConfirmed = new AtomicBoolean(false);
        this.sellerConfirmed = new AtomicBoolean(false);
    }

    // ─── Generate_ID ─────────────────────────────────────────────────────────

    /**
     * Generates a unique transaction ID.
     * Format: "BXN-" + first 8 chars of a UUID (uppercase).
     *
     * @return A new unique transaction ID string.
     */
    public String Generate_ID() {
        return "BXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ─── Authorization ───────────────────────────────────────────────────────

    /**
     * Validates and authorizes the provided payment information.
     *
     * Per project spec (Dr. Burris): No real payment verification is performed.
     * Checks only:
     *   - Payment method is not null
     *   - Agreed price is a positive number
     *   - Payment handle format if provided (no spaces, reasonable length)
     *
     * @param paymentInfo The payment info to authorize.
     * @return true if payment info passes validation, false otherwise.
     */
    public boolean Authorization(PaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            System.out.println("[Transaction " + bxnID + "] Authorization FAILED: paymentInfo is null.");
            return false;
        }

        if (paymentInfo.getMethod() == null) {
            System.out.println("[Transaction " + bxnID + "] Authorization FAILED: no payment method selected.");
            return false;
        }

        if (paymentInfo.getAgreedPrice() <= 0) {
            System.out.println("[Transaction " + bxnID + "] Authorization FAILED: agreed price must be greater than $0.00.");
            return false;
        }

        // Validate optional payment handle (e.g. phone number for Zelle, $tag for CashApp)
        if (paymentInfo.getPaymentHandle() != null && !paymentInfo.getPaymentHandle().isEmpty()) {
            if (!Auth_Payment_Method(paymentInfo)) {
                return false;
            }
        }

        System.out.println("[Transaction " + bxnID + "] Authorization PASSED for " + paymentInfo);
        return true;
    }

    // ─── Auth_Payment_Method ─────────────────────────────────────────────────

    /**
     * Validates the format of the payment handle for the selected method.
     *
     * NOTE: This does NOT verify real accounts or funds — only format correctness.
     *
     * @param paymentInfo The payment info containing method and handle.
     * @return true if format is valid, false otherwise.
     */
    public boolean Auth_Payment_Method(PaymentInfo paymentInfo) {
        if (paymentInfo == null || paymentInfo.getMethod() == null) {
            return false;
        }

        String handle = paymentInfo.getPaymentHandle();

        switch (paymentInfo.getMethod()) {
            case ZELLE:
                // Zelle uses phone number (10 digits) or email
                if (handle == null || handle.isEmpty()) return true; // handle is optional
                boolean isPhone = handle.matches("\\d{10}");
                boolean isEmail = handle.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
                if (!isPhone && !isEmail) {
                    System.out.println("[Transaction " + bxnID + "] Auth_Payment_Method FAILED: "
                            + "Zelle handle must be a 10-digit phone number or valid email. Got: " + handle);
                    return false;
                }
                break;

            case CASHAPP:
                // CashApp $cashtags: $ followed by 1-20 alphanumeric chars
                if (handle == null || handle.isEmpty()) return true;
                if (!handle.matches("^\\$[A-Za-z0-9_]{1,20}$")) {
                    System.out.println("[Transaction " + bxnID + "] Auth_Payment_Method FAILED: "
                            + "CashApp handle must start with '$' (e.g. $username). Got: " + handle);
                    return false;
                }
                break;

            case VENMO:
                // Venmo @ handles: @ followed by username
                if (handle == null || handle.isEmpty()) return true;
                if (!handle.matches("^@[A-Za-z0-9_-]{1,30}$")) {
                    System.out.println("[Transaction " + bxnID + "] Auth_Payment_Method FAILED: "
                            + "Venmo handle must start with '@' (e.g. @username). Got: " + handle);
                    return false;
                }
                break;

            case PAYPAL:
                // PayPal uses email or phone
                if (handle == null || handle.isEmpty()) return true;
                boolean isPayPalEmail = handle.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
                boolean isPayPalPhone = handle.matches("\\d{10,15}");
                if (!isPayPalEmail && !isPayPalPhone) {
                    System.out.println("[Transaction " + bxnID + "] Auth_Payment_Method FAILED: "
                            + "PayPal handle must be a valid email or phone number. Got: " + handle);
                    return false;
                }
                break;

            case CASH:
            case OTHER:
                // No handle validation needed for cash or other
                break;
        }

        System.out.println("[Transaction " + bxnID + "] Auth_Payment_Method PASSED for "
                + paymentInfo.getMethod() + " handle: " + (handle == null || handle.isEmpty() ? "(none)" : handle));
        return true;
    }

    // ─── Confirm_Payment_Type ────────────────────────────────────────────────

    /**
     * Confirms the payment type for the given transaction ID.
     * Verifies that the transaction exists and has a valid payment method set.
     *
     * @param bxnID The transaction ID to confirm payment type for.
     * @return true if payment type is confirmed, false otherwise.
     */
    public boolean Confirm_Payment_Type(String bxnID) {
        if (!this.bxnID.equals(bxnID)) {
            System.out.println("[Transaction] Confirm_Payment_Type FAILED: ID mismatch. Expected "
                    + this.bxnID + ", got " + bxnID);
            return false;
        }

        if (paymentInfo == null || paymentInfo.getMethod() == null) {
            System.out.println("[Transaction " + bxnID + "] Confirm_Payment_Type FAILED: no payment method on record.");
            return false;
        }

        System.out.println("[Transaction " + bxnID + "] Payment type confirmed: " + paymentInfo.getMethod());
        return true;
    }

    // ─── Status ──────────────────────────────────────────────────────────────

    /**
     * Returns the current status of the transaction.
     *
     * @param bxnID The transaction ID to check.
     * @return The current TransactionStatus, or null if ID does not match.
     */
    public TransactionStatus Status(String bxnID) {
        if (!this.bxnID.equals(bxnID)) {
            System.out.println("[Transaction] Status FAILED: ID mismatch.");
            return null;
        }
        return this.status;
    }

    // ─── Check_Sold_Status ───────────────────────────────────────────────────

    /**
     * Checks whether the listing associated with this transaction has been sold.
     *
     * @param listID The listing ID to check.
     * @return true if the listing is marked as sold (transaction COMPLETED), false otherwise.
     */
    public boolean Check_Sold_Status(String listID) {
        if (!this.listingID.equals(listID)) {
            System.out.println("[Transaction " + bxnID + "] Check_Sold_Status: listing ID does not match this transaction.");
            return false;
        }
        boolean sold = (this.status == TransactionStatus.COMPLETED);
        System.out.println("[Transaction " + bxnID + "] Check_Sold_Status for listing " + listID
                + ": " + (sold ? "SOLD" : "NOT YET SOLD"));
        return sold;
    }

    // ─── Release_Payment ─────────────────────────────────────────────────────

    /**
     * Marks the payment as released / transaction as completed.
     *
     * CommonGround does not move real money. "Releasing payment" here means:
     *   1. Both parties have confirmed the transaction.
     *   2. The listing is marked as sold in the database.
     *   3. The system transitions both users to the rating/review screen.
     *   4. An audit log entry is recorded.
     *
     * @param bxnID The transaction ID to release.
     * @return true if successfully released, false otherwise.
     */
    public boolean Release_Payment(String bxnID) {
        if (!this.bxnID.equals(bxnID)) {
            System.out.println("[Transaction] Release_Payment FAILED: ID mismatch.");
            return false;
        }

        if (this.status != TransactionStatus.BOTH_CONFIRMED) {
            System.out.println("[Transaction " + bxnID + "] Release_Payment FAILED: "
                    + "both parties must confirm before payment can be released. Current status: " + this.status);
            return false;
        }

        this.status = TransactionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();

        // Log to audit trail
        AuditTrail.LogTransaction(
                bxnID,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                buyerUserID + " & " + sellerUserID,
                "Transaction completed. Payment method: " + paymentInfo.getMethod()
                        + ". Agreed price: $" + String.format("%.2f", paymentInfo.getAgreedPrice())
                        + ". Both parties confirmed and are proceeding to rating/review."
        );

        // Persist to database
        TransactionDatabase.updateTransactionStatus(bxnID, TransactionStatus.COMPLETED);

        System.out.println("[Transaction " + bxnID + "] Payment RELEASED. "
                + "Both users proceeding to rating/review screen.");
        return true;
    }

    // ─── Multithreaded Confirmation (Core Feature) ───────────────────────────

    /**
     * Called by either the buyer or the seller when they press 'Confirm'.
     *
     * This is a MULTITHREADED operation. Both users must call this method.
     * Each call decrements a CountDownLatch(2). When both have confirmed,
     * the latch reaches zero and both threads are unblocked simultaneously,
     * moving both users to the rating/review screen.
     *
     * Thread safety: AtomicBoolean ensures each party can only confirm once.
     *
     * @param userID The ID of the user confirming (must be buyer or seller).
     * @return true if this user's confirmation was accepted, false otherwise.
     */
    public boolean confirmTransaction(String userID) {
        if (this.status == TransactionStatus.CANCELLED || this.status == TransactionStatus.FAILED) {
            System.out.println("[Transaction " + bxnID + "] confirmTransaction REJECTED: "
                    + "transaction is already " + this.status);
            return false;
        }

        synchronized (confirmLock) {
            if (userID.equals(buyerUserID)) {
                if (buyerConfirmed.getAndSet(true)) {
                    System.out.println("[Transaction " + bxnID + "] Buyer " + userID + " already confirmed.");
                    return false;
                }
                this.status = TransactionStatus.BUYER_CONFIRMED;
                this.updatedAt = LocalDateTime.now();
                System.out.println("[Transaction " + bxnID + "] Buyer " + userID + " confirmed. Waiting for seller...");

            } else if (userID.equals(sellerUserID)) {
                if (sellerConfirmed.getAndSet(true)) {
                    System.out.println("[Transaction " + bxnID + "] Seller " + userID + " already confirmed.");
                    return false;
                }
                this.status = TransactionStatus.SELLER_CONFIRMED;
                this.updatedAt = LocalDateTime.now();
                System.out.println("[Transaction " + bxnID + "] Seller " + userID + " confirmed. Waiting for buyer...");

            } else {
                System.out.println("[Transaction " + bxnID + "] confirmTransaction REJECTED: "
                        + "user " + userID + " is not part of this transaction.");
                return false;
            }
        }

        // Decrement the latch — when both parties call this, latch hits 0
        confirmationLatch.countDown();

        // Block this thread until the other party also confirms (or timeout)
        try {
            boolean bothConfirmed = confirmationLatch.await(CONFIRM_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (bothConfirmed) {
                synchronized (confirmLock) {
                    if (this.status != TransactionStatus.BOTH_CONFIRMED
                            && this.status != TransactionStatus.COMPLETED) {
                        this.status = TransactionStatus.BOTH_CONFIRMED;
                        this.updatedAt = LocalDateTime.now();
                    }
                }
                System.out.println("[Transaction " + bxnID + "] Both parties confirmed. "
                        + "User " + userID + " is now proceeding to the rating/review screen.");

                // Persist the both-confirmed status
                TransactionDatabase.updateTransactionStatus(bxnID, TransactionStatus.BOTH_CONFIRMED);

                // Log the event
                AuditTrail.LogTransaction(
                        bxnID,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        userID,
                        "User confirmed transaction. Both parties now confirmed — moving to rating/review."
                );

                return true;

            } else {
                // Timeout: the other party did not confirm in time
                System.out.println("[Transaction " + bxnID + "] TIMEOUT: the other party did not confirm within "
                        + CONFIRM_TIMEOUT_MINUTES + " minutes. Transaction remains in pending state.");
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Transaction " + bxnID + "] Thread interrupted while waiting for confirmation.");
            return false;
        }
    }

    /**
     * Cancels this transaction. Can only cancel if not yet BOTH_CONFIRMED or COMPLETED.
     *
     * @param userID The user requesting cancellation (must be buyer or seller).
     * @return true if cancelled successfully, false otherwise.
     */
    public boolean cancelTransaction(String userID) {
        if (!userID.equals(buyerUserID) && !userID.equals(sellerUserID)) {
            System.out.println("[Transaction " + bxnID + "] cancelTransaction REJECTED: "
                    + "user " + userID + " is not part of this transaction.");
            return false;
        }

        if (this.status == TransactionStatus.BOTH_CONFIRMED
                || this.status == TransactionStatus.COMPLETED) {
            System.out.println("[Transaction " + bxnID + "] cancelTransaction REJECTED: "
                    + "cannot cancel after both parties have confirmed.");
            return false;
        }

        this.status = TransactionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        // Release the latch so the other waiting thread is unblocked
        while (confirmationLatch.getCount() > 0) {
            confirmationLatch.countDown();
        }

        TransactionDatabase.updateTransactionStatus(bxnID, TransactionStatus.CANCELLED);

        AuditTrail.LogTransaction(
                bxnID,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                userID,
                "Transaction CANCELLED by user " + userID + "."
        );

        System.out.println("[Transaction " + bxnID + "] Transaction CANCELLED by user " + userID + ".");
        return true;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public String getBxnID()           { return bxnID; }
    public String getListingID()       { return listingID; }
    public String getBuyerUserID()     { return buyerUserID; }
    public String getSellerUserID()    { return sellerUserID; }
    public PaymentInfo getPaymentInfo(){ return paymentInfo; }
    public TransactionStatus getStatus(){ return status; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public boolean isBuyerConfirmed()  { return buyerConfirmed.get(); }
    public boolean isSellerConfirmed() { return sellerConfirmed.get(); }

    @Override
    public String toString() {
        return "Transaction{bxnID='" + bxnID + "'"
                + ", listingID='" + listingID + "'"
                + ", buyer='" + buyerUserID + "'"
                + ", seller='" + sellerUserID + "'"
                + ", status=" + status
                + ", payment=" + paymentInfo
                + "}";
    }
}
