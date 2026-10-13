"""
EXPERIMENT 5 : Case study - E-Commerce application
               Requirement analysis + NEGATIVE test scenarios & test cases.
"""
from ecommerce_app import Shop
from testlib import run_suite, attempt

CASE_STUDY = """
CASE STUDY : E-COMMERCE APPLICATION - REQUIREMENT ANALYSIS (see Experiment 4 for requirements R1-R8)

NEGATIVE TESTING = invalid, boundary-violating or out-of-sequence actions. The application must reject them with a
meaningful message and must not corrupt the cart, stock or order data.
Categories: A. Registration/Login   B. Search   C. Cart & stock   D. Coupons   E. Checkout & payment   F. Orders
"""

ADDR = {"name": "Arun", "street": "12 Gandhi Road", "city": "Coimbatore", "pincode": "641001"}
CARD = {"number": "4111111111111111", "expiry": "12/28", "cvv": "123"}

def shop():
    s = Shop(); s.register("Arun", "arun@mail.com", "Pass1234"); s.login("arun@mail.com", "Pass1234"); return s

def with_cart(pid="P101", qty=2):
    s = shop(); s.add_to_cart(pid, qty); return s

def pay(details, method="CARD", addr=ADDR):
    return attempt(with_cart().checkout, addr, method, details)

def dup_email():
    s = Shop(); s.register("Arun", "arun@mail.com", "Pass1234")
    return attempt(s.register, "Other", "arun@mail.com", "Diff5678")

def cancel_shipped():
    s = with_cart(); oid = s.checkout(ADDR, "COD"); s.ship_order(oid); return attempt(s.cancel_order, oid)

def cancel_twice():
    s = with_cart(); oid = s.checkout(ADDR, "COD"); s.cancel_order(oid); return attempt(s.cancel_order, oid)

def exceed_total():
    s = shop(); s.add_to_cart("P103", 4); return attempt(s.add_to_cart, "P103", 7)

def stock_unchanged_after_fail():
    s = with_cart(); attempt(s.checkout, ADDR, "CARD", {**CARD, "cvv": "12"}); return s.products["P101"]["stock"]

def cod_limit():
    s = shop(); s.products["P102"]["price"] = 30000.0; s.add_to_cart("P102", 2)   # 60000 + GST
    return attempt(s.checkout, ADDR, "COD")

cases = [
 # A. registration / login
 ("EN01","Register with invalid email","arun@mail","ERROR: Invalid email address", lambda: attempt(Shop().register,"Arun","arun@mail","Pass1234")),
 ("EN02","Register with weak password (no digit)","Password","ERROR: Password must be 8+ chars with letters and digits", lambda: attempt(Shop().register,"Arun","a@b.com","Password")),
 ("EN03","Register with short password (7 chars)","Pass123","ERROR: Password must be 8+ chars with letters and digits", lambda: attempt(Shop().register,"Arun","a@b.com","Pass123")),
 ("EN04","Register with blank name","'   '","ERROR: Name is required", lambda: attempt(Shop().register,"   ","a@b.com","Pass1234")),
 ("EN05","Register with an already used email","duplicate email","ERROR: Email already registered", dup_email),
 ("EN06","Login with wrong password","arun@mail.com / wrong","ERROR: Invalid email or password", lambda: attempt(shop().login,"arun@mail.com","wrong")),
 ("EN07","Login with unregistered email","none@mail.com","ERROR: Invalid email or password", lambda: attempt(Shop().login,"none@mail.com","Pass1234")),
 # B. search
 ("EN08","Search with one character","'m'","ERROR: Enter at least 2 characters", lambda: attempt(Shop().search,"m")),
 ("EN09","Search with empty keyword","''","ERROR: Enter at least 2 characters", lambda: attempt(Shop().search,"")),
 # C. cart & stock
 ("EN10","Add to cart without login","P101 x1","ERROR: Please login to continue", lambda: attempt(Shop().add_to_cart,"P101",1)),
 ("EN11","Add non-existent product","P999","ERROR: Product not found", lambda: attempt(shop().add_to_cart,"P999",1)),
 ("EN12","Quantity 0 (below boundary)","P101 x0","ERROR: Quantity must be between 1 and 10", lambda: attempt(shop().add_to_cart,"P101",0)),
 ("EN13","Quantity 11 (above boundary)","P103 x11","ERROR: Quantity must be between 1 and 10", lambda: attempt(shop().add_to_cart,"P103",11)),
 ("EN14","Negative quantity","P101 x-3","ERROR: Quantity must be between 1 and 10", lambda: attempt(shop().add_to_cart,"P101",-3)),
 ("EN15","Non-integer quantity","P101 x'two'","ERROR: Quantity must be between 1 and 10", lambda: attempt(shop().add_to_cart,"P101","two")),
 ("EN16","Quantity more than available stock","P102 x6 (stock 5)","ERROR: Only 5 left in stock", lambda: attempt(shop().add_to_cart,"P102",6)),
 ("EN17","Add out-of-stock product","P104 (stock 0)","ERROR: Out of stock", lambda: attempt(shop().add_to_cart,"P104",1)),
 ("EN18","Merged quantity exceeds 10 per product","4 + 7","ERROR: Maximum 10 units per product", exceed_total),
 ("EN19","Update quantity of item not in cart","P102","ERROR: Item not in cart", lambda: attempt(shop().update_qty,"P102",2)),
 ("EN20","Remove item that is not in cart","P102","ERROR: Item not in cart", lambda: attempt(shop().remove_item,"P102")),
 # D. coupons
 ("EN21","Coupon on empty cart","SAVE10","ERROR: Cart is empty", lambda: attempt(shop().apply_coupon,"SAVE10")),
 ("EN22","Invalid coupon code","HELLO99","ERROR: Invalid coupon code", lambda: attempt(with_cart().apply_coupon,"HELLO99")),
 ("EN23","Expired coupon","OLD50","ERROR: Coupon expired", lambda: attempt(with_cart().apply_coupon,"OLD50")),
 ("EN24","Order value below coupon minimum (599 < 1000)","SAVE10","ERROR: Minimum order value 1000 required", lambda: attempt(with_cart("P101",1).apply_coupon,"SAVE10")),
 # E. checkout & payment
 ("EN25","Checkout with empty cart","-","ERROR: Cart is empty", lambda: attempt(shop().checkout,ADDR,"COD")),
 ("EN26","Invalid pincode (5 digits)","64100","ERROR: Invalid pincode", lambda: attempt(with_cart().checkout,{**ADDR,"pincode":"64100"},"COD")),
 ("EN27","Pincode starting with 0","041001","ERROR: Invalid pincode", lambda: attempt(with_cart().checkout,{**ADDR,"pincode":"041001"},"COD")),
 ("EN28","Incomplete address (city missing)","city=''","ERROR: Address is incomplete", lambda: attempt(with_cart().checkout,{**ADDR,"city":""},"COD")),
 ("EN29","Card number fails Luhn check","4111111111111112","ERROR: Invalid card number", lambda: pay({**CARD,"number":"4111111111111112"})),
 ("EN30","Card number with 15 digits","411111111111111","ERROR: Invalid card number", lambda: pay({**CARD,"number":"411111111111111"})),
 ("EN31","Expired card","01/25","ERROR: Card expired", lambda: pay({**CARD,"expiry":"01/25"})),
 ("EN32","Invalid expiry format / month 13","13/28","ERROR: Invalid expiry format (MM/YY)", lambda: pay({**CARD,"expiry":"13/28"})),
 ("EN33","CVV with 2 digits","12","ERROR: Invalid CVV", lambda: pay({**CARD,"cvv":"12"})),
 ("EN34","Invalid UPI id","arun@123","ERROR: Invalid UPI ID", lambda: pay({"upi":"arun@123"},"UPI")),
 ("EN35","Unsupported payment method","BITCOIN","ERROR: Unsupported payment method", lambda: pay({},"BITCOIN")),
 ("EN36","COD above 50,000 not allowed","order > 50,000","ERROR: COD not available above 50000", cod_limit),
 ("EN37","Failed payment must NOT reduce stock","CVV invalid, stock stays 10","10", stock_unchanged_after_fail),
 # F. orders
 ("EN38","Cancel an order that is already shipped","status SHIPPED","ERROR: Order cannot be cancelled (status: SHIPPED)", cancel_shipped),
 ("EN39","Cancel an order twice","cancel ORD1001 twice","ERROR: Order cannot be cancelled (status: CANCELLED)", cancel_twice),
 ("EN40","Cancel non-existent order","ORD9999","ERROR: Order not found", lambda: attempt(shop().cancel_order,"ORD9999")),
]

if __name__ == "__main__":
    print(CASE_STUDY)
    run_suite("EXPERIMENT 5 - E-COMMERCE : NEGATIVE TEST CASES", cases)
