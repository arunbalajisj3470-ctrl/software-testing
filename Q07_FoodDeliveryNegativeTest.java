import java.util.List;

/**
 * Q7. Food Delivery Application - NEGATIVE test cases (TC-N01 .. TC-N20)
 * Run:  javac *.java  &&  java Q07_FoodDeliveryNegativeTest
 */
public class Q07_FoodDeliveryNegativeTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    /** The action must throw an AppException whose message contains the expected text. */
    static void expectError(String id, String description, String expectedText, Runnable action) {
        try {
            action.run();
            check(id, description + " (expected an error, none thrown)", false);
        } catch (FoodDeliveryApp.AppException e) {
            check(id, description + " -> \"" + e.getMessage() + "\"",
                    e.getMessage().toLowerCase().contains(expectedText.toLowerCase()));
        }
    }

    public static void main(String[] args) {
        final FoodDeliveryApp app = FoodDeliveryApp.withSampleData();
        final String mobile = "9876543210";

        // Pre-condition: one registered user
        app.register(mobile, "Arun", "Pass@123", app.sendOtp(mobile));

        // ---- Registration ----
        expectError("TC-N01", "9-digit mobile number rejected", "valid 10-digit",
                () -> app.sendOtp("987654321"));
        expectError("TC-N02", "Duplicate registration rejected", "already exists",
                () -> app.register(mobile, "Someone", "x", app.sendOtp("1111111111")));

        final String m2 = "9000000001";
        app.sendOtp(m2);
        expectError("TC-N03", "Wrong OTP rejected", "invalid otp",
                () -> app.register(m2, "Ravi", "pw", "000000"));
        app.expireOtp(m2);
        expectError("TC-N04", "Expired OTP rejected", "expired",
                () -> app.register(m2, "Ravi", "pw", "123456"));
        check("TC-N03b", "Account not created after failed OTP", !app.isRegistered(m2));

        // ---- Login ----
        expectError("TC-N05", "Wrong password rejected", "invalid credentials",
                () -> app.login(mobile, "wrong"));
        for (int i = 0; i < 4; i++) {
            try { app.login(mobile, "wrong"); } catch (FoodDeliveryApp.AppException ignored) { }
        }
        expectError("TC-N06", "Account locked after 5 wrong attempts", "locked",
                () -> app.login(mobile, "Pass@123"));

        // Use a second, healthy account for the remaining tests
        final String user = "9111111111";
        app.register(user, "Priya", "Pass@123", app.sendOtp(user));
        app.login(user, "Pass@123");

        // ---- Search ----
        expectError("TC-N07a", "Blank search rejected", "enter search text", () -> app.search("   "));
        List<FoodDeliveryApp.Restaurant> special = app.search("@#$%");
        check("TC-N07b", "Special-character search returns no results (no crash)", special.isEmpty());

        // ---- Cart ----
        expectError("TC-N08", "Out-of-stock item cannot be added", "unavailable",
                () -> app.addToCart("Green Leaf", "Gulab Jamun", 1));
        app.addToCart("Green Leaf", "Dal Tadka", 1);
        expectError("TC-N09a", "Negative quantity rejected", "quantity must be",
                () -> app.setQuantity("Dal Tadka", -1));
        expectError("TC-N09b", "Quantity above limit rejected", "quantity must be",
                () -> app.setQuantity("Dal Tadka", 999));

        // ---- Coupons ----
        expectError("TC-N10", "Expired coupon rejected", "expired", () -> app.applyCoupon("OLD20"));
        expectError("TC-N11", "Minimum order value enforced (cart Rs.150 < Rs.199)", "minimum order",
                () -> app.applyCoupon("WELCOME50"));
        expectError("TC-N11b", "Unknown coupon rejected", "invalid coupon", () -> app.applyCoupon("FAKE99"));

        // ---- Checkout ----
        app.addToCart("Green Leaf", "Paneer Butter Masala", 1);   // total now Rs.370
        expectError("TC-N13a", "Missing address rejected", "address required",
                () -> app.checkout("", "641001", "COD", null));
        expectError("TC-N13b", "Address outside delivery area rejected", "not available",
                () -> app.checkout("Far away", "999999", "COD", null));
        expectError("TC-N14", "Wrong UPI PIN fails payment", "payment failed",
                () -> app.checkout("12 Main St", "641001", "UPI", "0000"));
        check("TC-N14b", "No order created after failed payment", app.history().isEmpty());
        expectError("TC-N15", "Payment gateway timeout reported", "timeout",
                () -> app.checkout("12 Main St", "641001", "TIMEOUT", null));
        check("TC-N15b", "No order created after gateway timeout", app.history().isEmpty());

        // Coupon already used
        app.applyCoupon("WELCOME50");
        final FoodDeliveryApp.Order first = app.checkout("12 Main St", "641001", "UPI", "1234");
        app.addToCart("Green Leaf", "Paneer Butter Masala", 1);
        expectError("TC-N12", "Single-use coupon cannot be reused", "already used",
                () -> app.applyCoupon("WELCOME50"));

        // ---- Cancel after dispatch ----
        app.advance(first.id);
        app.advance(first.id);   // Out for Delivery
        expectError("TC-N16", "Cancel blocked once order is out for delivery", "cannot be cancelled",
                () -> app.cancel(first.id));

        // ---- Restaurant closed ----
        app.logout();
        app.login(user, "Pass@123");
        expectError("TC-N17", "Closed restaurant does not accept orders", "not accepting",
                () -> app.addToCart("Night Owl Cafe", "Sandwich", 1));

        // ---- Network loss during payment (modelled as gateway failure leaving state consistent) ----
        final int ordersBefore = app.history().size();
        app.addToCart("Green Leaf", "Dal Tadka", 1);
        try { app.checkout("12 Main St", "641001", "TIMEOUT", null); } catch (FoodDeliveryApp.AppException ignored) { }
        check("TC-N18", "No duplicate/partial order after interrupted payment", app.history().size() == ordersBefore);

        // ---- Security ----
        expectError("TC-N19", "SQL-injection text is treated as plain, invalid credentials", "invalid credentials",
                () -> app.login("' OR '1'='1", "x"));
        final String other = "9222222222";
        app.register(other, "Kiran", "Pass@123", app.sendOtp(other));
        final int firstId = first.id;
        app.logout();
        app.login(other, "Pass@123");
        expectError("TC-N20", "Cannot read another user's order by changing the ID", "access denied",
                () -> app.getOrder(firstId));

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
