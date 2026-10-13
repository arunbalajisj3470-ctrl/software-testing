"""Simple E-Commerce application (application under test for Q4 & Q5)."""
import re, datetime

class ShopError(Exception):
    pass

TODAY = datetime.date(2026, 10, 13)
COUPONS = {"SAVE10": {"type": "pct", "value": 10, "min": 1000, "expiry": datetime.date(2027, 12, 31)},
           "FLAT100": {"type": "flat", "value": 100, "min": 500, "expiry": datetime.date(2027, 12, 31)},
           "OLD50": {"type": "flat", "value": 50, "min": 100, "expiry": datetime.date(2025, 1, 1)}}

def luhn_ok(num):
    d = [int(c) for c in num][::-1]
    return (sum(d[0::2]) + sum(sum(divmod(2 * x, 10)) for x in d[1::2])) % 10 == 0

class Shop:
    def __init__(self):
        self.products = {"P101": {"name": "Wireless Mouse", "price": 599.0, "stock": 10},
                         "P102": {"name": "Mechanical Keyboard", "price": 1499.0, "stock": 5},
                         "P103": {"name": "USB Cable", "price": 199.0, "stock": 100},
                         "P104": {"name": "Laptop Stand", "price": 1299.0, "stock": 0}}
        self.users, self.user, self.cart, self.coupon = {}, None, {}, None
        self.orders, self.fails = {}, {}

    # ---- account
    def register(self, name, email, password):
        if not name or not name.strip(): raise ShopError("Name is required")
        if not re.fullmatch(r"[\w.+-]+@[\w-]+\.[A-Za-z]{2,}", email or ""): raise ShopError("Invalid email address")
        if not re.fullmatch(r"(?=.*[A-Za-z])(?=.*\d).{8,}", password or ""): raise ShopError("Password must be 8+ chars with letters and digits")
        if email in self.users: raise ShopError("Email already registered")
        self.users[email] = {"name": name, "pwd": password}
        return "Registration successful"

    def login(self, email, password):
        if email not in self.users or self.users[email]["pwd"] != password: raise ShopError("Invalid email or password")
        self.user = email; return f"Welcome {self.users[email]['name']}"

    def _need_login(self):
        if not self.user: raise ShopError("Please login to continue")

    # ---- catalogue
    def search(self, keyword):
        if len((keyword or "").strip()) < 2: raise ShopError("Enter at least 2 characters")
        return [p["name"] for p in self.products.values() if keyword.strip().lower() in p["name"].lower()]

    # ---- cart
    def add_to_cart(self, pid, qty):
        self._need_login()
        if pid not in self.products: raise ShopError("Product not found")
        if isinstance(qty, bool) or not isinstance(qty, int) or not 1 <= qty <= 10: raise ShopError("Quantity must be between 1 and 10")
        stock = self.products[pid]["stock"]
        if stock == 0: raise ShopError("Out of stock")
        new = self.cart.get(pid, 0) + qty
        if new > stock: raise ShopError(f"Only {stock} left in stock")
        if new > 10: raise ShopError("Maximum 10 units per product")
        self.cart[pid] = new; return f"Added: {self.products[pid]['name']} x{new}"

    def update_qty(self, pid, qty):
        self._need_login()
        if pid not in self.cart: raise ShopError("Item not in cart")
        if not isinstance(qty, int) or not 1 <= qty <= 10: raise ShopError("Quantity must be between 1 and 10")
        if qty > self.products[pid]["stock"]: raise ShopError(f"Only {self.products[pid]['stock']} left in stock")
        self.cart[pid] = qty; return "Cart updated"

    def remove_item(self, pid):
        self._need_login()
        if pid not in self.cart: raise ShopError("Item not in cart")
        del self.cart[pid]; return "Item removed"

    def subtotal(self): return sum(self.products[p]["price"] * q for p, q in self.cart.items())

    def apply_coupon(self, code):
        self._need_login()
        if not self.cart: raise ShopError("Cart is empty")
        c = COUPONS.get((code or "").upper())
        if not c: raise ShopError("Invalid coupon code")
        if c["expiry"] < TODAY: raise ShopError("Coupon expired")
        if self.subtotal() < c["min"]: raise ShopError(f"Minimum order value {c['min']} required")
        self.coupon = code.upper(); return f"Coupon {self.coupon} applied"

    def totals(self):
        sub = self.subtotal(); disc = 0.0
        if self.coupon:
            c = COUPONS[self.coupon]; disc = sub * c["value"] / 100 if c["type"] == "pct" else c["value"]
        taxable = sub - disc
        gst = taxable * 0.18
        ship = 0.0 if taxable >= 500 else 40.0
        return {"subtotal": round(sub, 2), "discount": round(disc, 2), "gst": round(gst, 2),
                "shipping": ship, "total": round(taxable + gst + ship, 2)}

    # ---- checkout
    def checkout(self, address, method, details=None):
        self._need_login()
        if not self.cart: raise ShopError("Cart is empty")
        if not re.fullmatch(r"[1-9]\d{5}", str(address.get("pincode", ""))): raise ShopError("Invalid pincode")
        if not address.get("street") or not address.get("city"): raise ShopError("Address is incomplete")
        total = self.totals()["total"]; details = details or {}
        if method == "CARD":
            n = str(details.get("number", "")).replace(" ", "")
            if not re.fullmatch(r"\d{16}", n) or not luhn_ok(n): raise ShopError("Invalid card number")
            m = re.fullmatch(r"(0[1-9]|1[0-2])/(\d{2})", str(details.get("expiry", "")))
            if not m: raise ShopError("Invalid expiry format (MM/YY)")
            if (2000 + int(m.group(2)), int(m.group(1))) < (TODAY.year, TODAY.month): raise ShopError("Card expired")
            if not re.fullmatch(r"\d{3}", str(details.get("cvv", ""))): raise ShopError("Invalid CVV")
        elif method == "UPI":
            if not re.fullmatch(r"[\w.\-]{2,}@[A-Za-z]{2,}", str(details.get("upi", ""))): raise ShopError("Invalid UPI ID")
        elif method == "COD":
            if total > 50000: raise ShopError("COD not available above 50000")
        else:
            raise ShopError("Unsupported payment method")
        oid = f"ORD{1001 + len(self.orders)}"
        for p, q in self.cart.items(): self.products[p]["stock"] -= q
        self.orders[oid] = {"items": dict(self.cart), "total": total, "status": "PLACED", "method": method}
        self.cart, self.coupon = {}, None
        return oid

    def ship_order(self, oid): self.orders[oid]["status"] = "SHIPPED"

    def cancel_order(self, oid):
        self._need_login()
        if oid not in self.orders: raise ShopError("Order not found")
        o = self.orders[oid]
        if o["status"] != "PLACED": raise ShopError(f"Order cannot be cancelled (status: {o['status']})")
        for p, q in o["items"].items(): self.products[p]["stock"] += q
        o["status"] = "CANCELLED"; return "Order cancelled"
