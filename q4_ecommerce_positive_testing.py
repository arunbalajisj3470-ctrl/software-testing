"""
EXPERIMENT 4 : Case study - E-Commerce application
               Requirement analysis + POSITIVE test scenarios & test cases.
"""
from ecommerce_app import Shop
from testlib import run_suite

CASE_STUDY = """
CASE STUDY : E-COMMERCE APPLICATION - REQUIREMENT ANALYSIS
Actors      : Customer (guest / registered), System (payment, inventory)
Requirements:
  R1  Registration: name, valid email, password (8+ chars, letters+digits), unique email.
  R2  Login with email & password.
  R3  Search products by keyword (min 2 characters).
  R4  Cart: add (qty 1-10, within stock), merge same product, update quantity, remove item.
  R5  Coupons: SAVE10 = 10% off (min order 1000); FLAT100 = 100 off (min order 500).
  R6  Price: GST 18% on discounted amount; shipping 40 if amount < 500, else free.
  R7  Checkout with valid 6-digit pincode and payment: CARD (16-digit Luhn-valid, MM/YY not expired, 3-digit CVV),
      UPI (id@bank) or COD (<= 50,000).
  R8  Order gets ID, stock is reduced; order can be cancelled only while status = PLACED (stock restored).

POSITIVE TESTING = valid data & normal user flow; every operation must succeed with correct results.
"""

ADDR = {"name": "Arun", "street": "12 Gandhi Road", "city": "Coimbatore", "pincode": "641001"}
CARD = {"number": "4111 1111 1111 1111", "expiry": "12/28", "cvv": "123"}

def shop():
    s = Shop(); s.register("Arun", "arun@mail.com", "Pass1234"); s.login("arun@mail.com", "Pass1234"); return s

def merge():
    s = shop(); s.add_to_cart("P101", 2); return s.add_to_cart("P101", 3)

def coupon_total():
    s = shop(); s.add_to_cart("P101", 2); s.apply_coupon("SAVE10"); return s.totals()

def flat():
    s = shop(); s.add_to_cart("P101", 1); s.apply_coupon("FLAT100"); return s.totals()["discount"]

def ship_small():
    s = shop(); s.add_to_cart("P103", 1); return s.totals()

def ship_free():
    s = shop(); s.add_to_cart("P101", 1); return s.totals()["shipping"]

def order_card():
    s = shop(); s.add_to_cart("P101", 2); oid = s.checkout(ADDR, "CARD", CARD)
    return f"{oid} | status={s.orders[oid]['status']} | stock P101={s.products['P101']['stock']} | cart={s.cart}"

def order_upi():
    s = shop(); s.add_to_cart("P103", 3); return s.checkout(ADDR, "UPI", {"upi": "arun@okbank"})

def order_cod():
    s = shop(); s.add_to_cart("P102", 1); return s.checkout(ADDR, "COD")

def cancel():
    s = shop(); s.add_to_cart("P101", 2); oid = s.checkout(ADDR, "COD")
    return f"{s.cancel_order(oid)} | stock P101={s.products['P101']['stock']}"

def update():
    s = shop(); s.add_to_cart("P102", 1); return s.update_qty("P102", 4) + f" | subtotal={s.subtotal()}"

def remove():
    s = shop(); s.add_to_cart("P102", 1); return s.remove_item("P102") + f" | cart={s.cart}"

cases = [
 ("EP01","Register with valid details","Arun, arun@mail.com, Pass1234","Registration successful", lambda: Shop().register("Arun","arun@mail.com","Pass1234")),
 ("EP02","Login with registered credentials","arun@mail.com / Pass1234","Welcome Arun", lambda: (lambda s:(s.register("Arun","arun@mail.com","Pass1234"), s.login("arun@mail.com","Pass1234"))[1])(Shop())),
 ("EP03","Search product by keyword (case-insensitive)","'mouse'","['Wireless Mouse']", lambda: Shop().search("mouse")),
 ("EP04","Search with no match returns empty list","'tablet'","[]", lambda: Shop().search("tablet")),
 ("EP05","Add product to cart","P101 x2","Added: Wireless Mouse x2", lambda: shop().add_to_cart("P101",2)),
 ("EP06","Adding same product merges quantity","P101 x2 then x3","Added: Wireless Mouse x5", merge),
 ("EP07","Boundary - add maximum quantity 10","P103 x10","Added: USB Cable x10", lambda: shop().add_to_cart("P103",10)),
 ("EP08","Boundary - add exactly the remaining stock (5)","P102 x5","Added: Mechanical Keyboard x5", lambda: shop().add_to_cart("P102",5)),
 ("EP09","Update cart quantity","P102 1 -> 4","Cart updated | subtotal=5996.0", update),
 ("EP10","Remove item from cart","remove P102","Item removed | cart={}", remove),
 ("EP11","Apply SAVE10 on order of 1198 and verify price breakup","2 x 599, SAVE10",
        "{'subtotal': 1198.0, 'discount': 119.8, 'gst': 194.08, 'shipping': 0.0, 'total': 1272.28}", coupon_total),
 ("EP12","Apply FLAT100 coupon (order 599 >= min 500)","1 x 599, FLAT100","100", flat),
 ("EP13","Shipping charge 40 applied for order below 500","USB Cable 199",
        "{'subtotal': 199.0, 'discount': 0.0, 'gst': 35.82, 'shipping': 40.0, 'total': 274.82}", ship_small),
 ("EP14","Free shipping for order of 500 or more","Mouse 599","0.0", ship_free),
 ("EP15","Checkout with valid card; order placed, stock reduced, cart cleared","Visa 4111..1111, 12/28, 123",
        "ORD1001 | status=PLACED | stock P101=8 | cart={}", order_card),
 ("EP16","Checkout using valid UPI id","arun@okbank","ORD1001", order_upi),
 ("EP17","Checkout with Cash on Delivery (<= 50,000)","COD","ORD1001", order_cod),
 ("EP18","Cancel a PLACED order; stock restored","cancel ORD1001","Order cancelled | stock P101=10", cancel),
]

if __name__ == "__main__":
    print(CASE_STUDY)
    run_suite("EXPERIMENT 4 - E-COMMERCE : POSITIVE TEST CASES", cases)
