import java.util.List;

/**
 * Q6. Food Delivery Application - POSITIVE test cases (TC-P01 .. TC-P15)
 * Run:  javac *.java  &&  java Q06_FoodDeliveryPositiveTest
 */
public class Q06_FoodDeliveryPositiveTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    public static void main(String[] args) {
        FoodDeliveryApp app = FoodDeliveryApp.withSampleData();
        String mobile = "9876543210";

        // TC-P01 Registration with valid details and OTP
        String otp = app.sendOtp(mobile);
        app.register(mobile, "Arun", "Pass@123", otp);
        check("TC-P01", "Register new user with valid details and OTP", app.isRegistered(mobile));

        // TC-P02 Login
        app.login(mobile, "Pass@123");
        check("TC-P02", "Login with valid credentials", mobile.equals(app.currentUser()));

        // TC-P03 Search by keyword
        List<FoodDeliveryApp.Restaurant> found = app.search("Biryani");
        check("TC-P03", "Search 'Biryani' lists Biryani House",
                found.size() == 1 && found.get(0).name.equals("Biryani House"));

        // TC-P04 Filters (veg + rating 4+)
        List<FoodDeliveryApp.Restaurant> filtered = app.search("Indian", true, 4.0);
        check("TC-P04", "Filter Veg + rating 4.0+ returns only Green Leaf",
                filtered.size() == 1 && filtered.get(0).name.equals("Green Leaf"));

        // TC-P05 Add item to cart
        app.addToCart("Green Leaf", "Paneer Butter Masala", 1);
        check("TC-P05", "Add item to cart, cart count = 1", app.cartCount() == 1);

        // TC-P06 Update quantity and remove item
        app.addToCart("Green Leaf", "Dal Tadka", 1);
        app.setQuantity("Paneer Butter Masala", 3);
        check("TC-P06a", "Quantity updated to 3, total = 3*220 + 150 = 810", app.itemTotal() == 810.0);
        app.removeFromCart("Dal Tadka");
        check("TC-P06b", "Removed item disappears from cart", !app.cartContains("Dal Tadka") && app.itemTotal() == 660.0);

        // TC-P07 Valid coupon
        double before = app.bill();
        app.applyCoupon("WELCOME50");
        check("TC-P07", "Coupon WELCOME50 reduces bill by 50", Math.abs((before - app.bill()) - 50.0) < 0.001);

        // TC-P08 Checkout with UPI
        FoodDeliveryApp.Order o1 = app.checkout("12, Gandhi Road, Coimbatore", "641001", "UPI", "1234");
        check("TC-P08", "UPI payment success, order confirmed with ID", o1.id > 0 && o1.paid && "Confirmed".equals(o1.status));

        // TC-P09 Cash on delivery
        app.addToCart("Biryani House", "Chicken Biryani", 2);
        FoodDeliveryApp.Order o2 = app.checkout("12, Gandhi Road, Coimbatore", "641001", "COD", null);
        check("TC-P09", "COD order placed with status Confirmed", !o2.paid && "Confirmed".equals(o2.status));

        // TC-P10 Tracking through all statuses
        StringBuilder flow = new StringBuilder(o1.status);
        for (int i = 0; i < 3; i++) { app.advance(o1.id); flow.append(" > ").append(app.getOrder(o1.id).status); }
        check("TC-P10", "Status flow: " + flow,
                flow.toString().equals("Confirmed > Preparing > Out for Delivery > Delivered"));

        // TC-P11 Live tracking is modelled by the 'Out for Delivery' status (covered in TC-P10)
        check("TC-P11", "Order reaches 'Delivered' after 'Out for Delivery'", "Delivered".equals(app.getOrder(o1.id).status));

        // TC-P12 Cancel before acceptance + refund for prepaid
        app.addToCart("Green Leaf", "Dal Tadka", 1);
        FoodDeliveryApp.Order o3 = app.checkout("12, Gandhi Road, Coimbatore", "641001", "UPI", "1234");
        app.cancel(o3.id);
        check("TC-P12", "Cancel confirmed order; refund initiated for prepaid order",
                "Cancelled".equals(o3.status) && o3.refunded);

        // TC-P13 Rating
        app.rate(o1.id, 5);
        check("TC-P13", "Rating of 5 stars saved on delivered order", app.getOrder(o1.id).rating == 5);

        // TC-P14 History and reorder
        check("TC-P14a", "Order history shows 3 orders", app.history().size() == 3);
        app.reorder(o1.id);
        check("TC-P14b", "Reorder puts same items (3 x Paneer) back in cart", app.cartQuantity("Paneer Butter Masala") == 3);

        // TC-P15 Notifications
        boolean confirmedMsg = app.notifications().contains("Order #" + o1.id + " Confirmed");
        boolean deliveredMsg = app.notifications().contains("Order #" + o1.id + " Delivered");
        check("TC-P15", "Notification sent for each status change", confirmedMsg && deliveredMsg);

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
