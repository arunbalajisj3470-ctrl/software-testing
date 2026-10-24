import java.util.*;

/**
 * Simple in-memory model of a Food Delivery Application.
 * Used by Q06 (positive) and Q07 (negative) test programs.
 */
public class FoodDeliveryApp {

    public static class AppException extends RuntimeException {
        public AppException(String message) { super(message); }
    }

    public static class Item {
        public final String name;
        public final double price;
        public final boolean veg;
        public boolean available;
        public Item(String name, double price, boolean veg, boolean available) {
            this.name = name; this.price = price; this.veg = veg; this.available = available;
        }
    }

    public static class Restaurant {
        public final String name, cuisine;
        public final double rating;
        public final boolean pureVeg;
        public boolean open;
        public final Map<String, Item> menu = new LinkedHashMap<>();
        public Restaurant(String name, String cuisine, double rating, boolean pureVeg, boolean open) {
            this.name = name; this.cuisine = cuisine; this.rating = rating; this.pureVeg = pureVeg; this.open = open;
        }
        public Restaurant add(Item i) { menu.put(i.name, i); return this; }
    }

    public static class Order {
        public final int id;
        public final String owner, restaurant, payment;
        public final Map<String, Integer> items;
        public final double total;
        public String status = "Confirmed";
        public boolean paid, refunded;
        public int rating;
        Order(int id, String owner, String restaurant, Map<String, Integer> items, double total, String payment, boolean paid) {
            this.id = id; this.owner = owner; this.restaurant = restaurant;
            this.items = items; this.total = total; this.payment = payment; this.paid = paid;
        }
    }

    private static class User {
        String name, password;
        int failedAttempts;
        boolean locked;
        Set<String> usedCoupons = new HashSet<>();
    }

    private static class Coupon {
        final double discount, minOrder; final boolean expired;
        Coupon(double discount, double minOrder, boolean expired) {
            this.discount = discount; this.minOrder = minOrder; this.expired = expired;
        }
    }

    public static final int MAX_QTY = 20;
    public static final double DELIVERY_FEE = 30.0;
    public static final double TAX_RATE = 0.05;

    private final Map<String, Restaurant> restaurants = new LinkedHashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, String> otps = new HashMap<>();
    private final Set<String> expiredOtps = new HashSet<>();
    private final Map<String, Coupon> coupons = new HashMap<>();
    private final Set<String> serviceablePins = new HashSet<>(Arrays.asList("600001", "641001", "602105"));
    private final Map<Integer, Order> orders = new LinkedHashMap<>();
    private final Map<String, Map<String, Integer>> carts = new HashMap<>();
    private final Map<String, String> cartRestaurant = new HashMap<>();
    private final Map<String, String> appliedCoupon = new HashMap<>();
    private final List<String> notifications = new ArrayList<>();
    private String currentUser;
    private int nextOrderId = 1001;

    public FoodDeliveryApp() {
        coupons.put("WELCOME50", new Coupon(50, 199, false));
        coupons.put("OLD20", new Coupon(20, 0, true));
        coupons.put("BIG100", new Coupon(100, 299, false));
    }

    public static FoodDeliveryApp withSampleData() {
        FoodDeliveryApp app = new FoodDeliveryApp();
        app.addRestaurant(new Restaurant("Biryani House", "Biryani", 4.3, false, true)
                .add(new Item("Chicken Biryani", 250, false, true))
                .add(new Item("Veg Biryani", 180, true, true)));
        app.addRestaurant(new Restaurant("Green Leaf", "North Indian", 4.5, true, true)
                .add(new Item("Paneer Butter Masala", 220, true, true))
                .add(new Item("Dal Tadka", 150, true, true))
                .add(new Item("Gulab Jamun", 80, true, false)));
        app.addRestaurant(new Restaurant("Night Owl Cafe", "Cafe", 3.8, false, false)
                .add(new Item("Sandwich", 120, true, true)));
        return app;
    }

    public void addRestaurant(Restaurant r) { restaurants.put(r.name, r); }
    public Restaurant restaurant(String name) { return restaurants.get(name); }

    // ---------- Registration / login ----------
    private static void validateMobile(String mobile) {
        if (mobile == null || !mobile.matches("\\d{10}"))
            throw new AppException("Enter a valid 10-digit mobile number");
    }

    public String sendOtp(String mobile) {
        validateMobile(mobile);
        expiredOtps.remove(mobile);
        otps.put(mobile, "123456");
        return "123456";
    }

    public void expireOtp(String mobile) { expiredOtps.add(mobile); }

    public void register(String mobile, String name, String password, String otp) {
        validateMobile(mobile);
        if (users.containsKey(mobile)) throw new AppException("Account already exists");
        if (expiredOtps.contains(mobile)) throw new AppException("OTP expired");
        if (otp == null || !otp.equals(otps.get(mobile))) throw new AppException("Invalid OTP");
        User u = new User(); u.name = name; u.password = password;
        users.put(mobile, u);
    }

    public boolean isRegistered(String mobile) { return users.containsKey(mobile); }

    public void login(String mobile, String password) {
        User u = users.get(mobile);
        if (u == null) throw new AppException("Invalid credentials");
        if (u.locked) throw new AppException("Account temporarily locked");
        if (!u.password.equals(password)) {
            u.failedAttempts++;
            if (u.failedAttempts >= 5) { u.locked = true; throw new AppException("Account temporarily locked"); }
            throw new AppException("Invalid credentials");
        }
        u.failedAttempts = 0;
        currentUser = mobile;
    }

    public void logout() { currentUser = null; }
    public String currentUser() { return currentUser; }
    private void requireLogin() { if (currentUser == null) throw new AppException("Please login"); }

    // ---------- Search ----------
    public List<Restaurant> search(String keyword, boolean vegOnly, double minRating) {
        requireLogin();
        if (keyword == null || keyword.trim().isEmpty()) throw new AppException("Enter search text");
        String k = keyword.trim().toLowerCase();
        List<Restaurant> result = new ArrayList<>();
        for (Restaurant r : restaurants.values()) {
            boolean match = r.name.toLowerCase().contains(k) || r.cuisine.toLowerCase().contains(k);
            for (Item i : r.menu.values()) if (i.name.toLowerCase().contains(k)) match = true;
            if (match && (!vegOnly || r.pureVeg) && r.rating >= minRating) result.add(r);
        }
        return result;
    }

    public List<Restaurant> search(String keyword) { return search(keyword, false, 0); }

    // ---------- Cart ----------
    private Map<String, Integer> cart() {
        return carts.computeIfAbsent(currentUser, k -> new LinkedHashMap<>());
    }

    public void addToCart(String restaurantName, String itemName, int qty) {
        requireLogin();
        Restaurant r = restaurants.get(restaurantName);
        if (r == null) throw new AppException("Restaurant not found");
        if (!r.open) throw new AppException("Restaurant not accepting orders");
        Item it = r.menu.get(itemName);
        if (it == null) throw new AppException("Item not found");
        if (!it.available) throw new AppException("Currently unavailable");
        Map<String, Integer> c = cart();
        String existing = cartRestaurant.get(currentUser);
        if (!c.isEmpty() && existing != null && !existing.equals(restaurantName))
            throw new AppException("Cart has items from another restaurant");
        int newQty = c.getOrDefault(itemName, 0) + qty;
        if (qty < 1 || newQty > MAX_QTY) throw new AppException("Quantity must be between 1 and " + MAX_QTY);
        c.put(itemName, newQty);
        cartRestaurant.put(currentUser, restaurantName);
    }

    public void setQuantity(String itemName, int qty) {
        requireLogin();
        Map<String, Integer> c = cart();
        if (!c.containsKey(itemName)) throw new AppException("Item not in cart");
        if (qty < 1 || qty > MAX_QTY) throw new AppException("Quantity must be between 1 and " + MAX_QTY);
        c.put(itemName, qty);
    }

    public void removeFromCart(String itemName) { requireLogin(); cart().remove(itemName); }
    public int cartCount() { requireLogin(); int n = 0; for (int q : cart().values()) n += q; return n; }
    public boolean cartContains(String item) { return cart().containsKey(item); }
    public int cartQuantity(String item) { return cart().getOrDefault(item, 0); }

    public double itemTotal() {
        requireLogin();
        Restaurant r = restaurants.get(cartRestaurant.get(currentUser));
        double t = 0;
        if (r == null) return 0;
        for (Map.Entry<String, Integer> e : cart().entrySet()) t += r.menu.get(e.getKey()).price * e.getValue();
        return t;
    }

    // ---------- Coupons and bill ----------
    public void applyCoupon(String code) {
        requireLogin();
        Coupon c = coupons.get(code);
        if (c == null) throw new AppException("Invalid coupon");
        if (c.expired) throw new AppException("Coupon expired");
        if (itemTotal() < c.minOrder) throw new AppException("Minimum order value not met");
        if (users.get(currentUser).usedCoupons.contains(code)) throw new AppException("Coupon already used");
        appliedCoupon.put(currentUser, code);
    }

    public double bill() {
        double items = itemTotal();
        double discount = appliedCoupon.containsKey(currentUser) ? coupons.get(appliedCoupon.get(currentUser)).discount : 0;
        return Math.max(0, items + items * TAX_RATE + DELIVERY_FEE - discount);
    }

    // ---------- Checkout / orders ----------
    public Order checkout(String address, String pincode, String paymentMethod, String paymentDetail) {
        requireLogin();
        if (cart().isEmpty()) throw new AppException("Cart is empty");
        if (address == null || address.trim().isEmpty()) throw new AppException("Delivery address required");
        if (!serviceablePins.contains(pincode)) throw new AppException("Delivery not available at this location");
        String rName = cartRestaurant.get(currentUser);
        if (!restaurants.get(rName).open) throw new AppException("Restaurant not accepting orders");
        boolean paid;
        if ("COD".equals(paymentMethod)) paid = false;
        else if ("UPI".equals(paymentMethod)) {
            if (!"1234".equals(paymentDetail)) throw new AppException("Payment failed: invalid UPI PIN");
            paid = true;
        } else if ("TIMEOUT".equals(paymentMethod)) throw new AppException("Payment gateway timeout");
        else throw new AppException("Unsupported payment method");

        double total = bill();
        Order o = new Order(nextOrderId++, currentUser, rName, new LinkedHashMap<>(cart()), total, paymentMethod, paid);
        orders.put(o.id, o);
        if (appliedCoupon.containsKey(currentUser))
            users.get(currentUser).usedCoupons.add(appliedCoupon.remove(currentUser));
        cart().clear();
        notifications.add("Order #" + o.id + " Confirmed");
        return o;
    }

    private static final String[] FLOW = {"Confirmed", "Preparing", "Out for Delivery", "Delivered"};

    /** Restaurant / delivery-partner side action. */
    public void advance(int orderId) {
        Order o = orders.get(orderId);
        if (o == null) throw new AppException("Order not found");
        for (int i = 0; i < FLOW.length - 1; i++) {
            if (FLOW[i].equals(o.status)) {
                o.status = FLOW[i + 1];
                notifications.add("Order #" + o.id + " " + o.status);
                return;
            }
        }
        throw new AppException("Order cannot be advanced");
    }

    public Order getOrder(int orderId) {
        requireLogin();
        Order o = orders.get(orderId);
        if (o == null || !o.owner.equals(currentUser)) throw new AppException("Access denied");
        return o;
    }

    public void cancel(int orderId) {
        Order o = getOrder(orderId);
        if (!"Confirmed".equals(o.status)) throw new AppException("Order cannot be cancelled now");
        o.status = "Cancelled";
        if (o.paid) o.refunded = true;
        notifications.add("Order #" + o.id + " Cancelled");
    }

    public void rate(int orderId, int stars) {
        Order o = getOrder(orderId);
        if (!"Delivered".equals(o.status)) throw new AppException("Only delivered orders can be rated");
        if (stars < 1 || stars > 5) throw new AppException("Rating must be between 1 and 5");
        o.rating = stars;
    }

    public List<Order> history() {
        requireLogin();
        List<Order> list = new ArrayList<>();
        for (Order o : orders.values()) if (o.owner.equals(currentUser)) list.add(o);
        return list;
    }

    /** Re-adds the items of an old order to the cart at current prices. */
    public void reorder(int orderId) {
        Order o = getOrder(orderId);
        cart().clear();
        cartRestaurant.remove(currentUser);
        for (Map.Entry<String, Integer> e : o.items.entrySet()) addToCart(o.restaurant, e.getKey(), e.getValue());
    }

    public List<String> notifications() { return notifications; }
}
