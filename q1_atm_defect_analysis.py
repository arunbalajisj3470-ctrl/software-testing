"""
EXPERIMENT 1 : Examine the specification of an ATM system and identify / document software defects.

Approach
  1. Write the ATM SYSTEM SPECIFICATION (what the software must do).
  2. Take the implementation under test (it contains hidden, realistic defects).
  3. Derive test cases from every specification clause (incl. boundary values).
  4. Run them; every failed test is logged as a DEFECT in a defect report.
"""

class ATMError(Exception):
    pass

SPEC = """
ATM SYSTEM SPECIFICATION
  S1  Card holder is authenticated with a 4-digit PIN. Account is LOCKED after 3 consecutive wrong PINs.
  S2  Withdrawal amount must be a multiple of 100, minimum 100, maximum 10,000 per transaction.
  S3  Daily withdrawal limit is 20,000 per account.
  S4  A minimum balance of 500 must always remain in the account after a withdrawal.
  S5  Deposit amount must be a positive multiple of 100.
  S6  PIN change: new PIN must be exactly 4 digits, numeric, and different from the old PIN.
  S7  Fund transfer to the same account is not allowed; destination must exist; balance rules (S4) apply.
  S8  Mini statement shows the LAST 5 transactions.
  S9  After logout no transaction is allowed until the user authenticates again.
"""

# ------------------------------------------------------------------ system under test (contains defects)
class ATM:
    MAX_ATTEMPTS = 3
    def __init__(self, accounts):
        self.accounts = accounts
        self.acct = None
        self.attempts = {}
        self.locked = set()
        self.history = []
        self.withdrawn_today = {}

    def login(self, acct, pin):
        if acct not in self.accounts: raise ATMError("Invalid account")
        if acct in self.locked: raise ATMError("Account locked")
        if self.accounts[acct]["pin"] != pin:
            self.attempts[acct] = self.attempts.get(acct, 0) + 1
            if self.attempts[acct] > self.MAX_ATTEMPTS:          # DEFECT: should lock at ==3 (off-by-one)
                self.locked.add(acct)
            raise ATMError("Wrong PIN")
        self.attempts[acct] = 0
        self.acct = acct

    def logout(self):
        self.history.append("LOGOUT")                            # DEFECT: session (self.acct) is not cleared

    def _need_login(self):
        if self.acct is None: raise ATMError("Not logged in")

    def withdraw(self, amount):
        self._need_login()
        if amount < 0 or amount % 100 != 0: raise ATMError("Amount must be multiple of 100")  # DEFECT: 0 accepted
        if amount > 10000: raise ATMError("Per-transaction limit exceeded")
        a = self.accounts[self.acct]
        if amount > a["balance"]: raise ATMError("Insufficient funds")   # DEFECT: min balance 500 ignored
        a["balance"] -= amount                                    # DEFECT: daily limit never checked
        self.history.append(f"WD {amount}")
        return a["balance"]

    def deposit(self, amount):
        self._need_login()
        if amount % 100 != 0: raise ATMError("Amount must be multiple of 100")  # DEFECT: negative accepted
        self.accounts[self.acct]["balance"] += amount
        self.history.append(f"DEP {amount}")
        return self.accounts[self.acct]["balance"]

    def change_pin(self, new_pin):
        self._need_login()
        self.accounts[self.acct]["pin"] = new_pin                # DEFECT: no length / numeric / same-as-old check
        self.history.append("PIN CHANGE")

    def transfer(self, to_acct, amount):
        self._need_login()
        if to_acct not in self.accounts: raise ATMError("Destination not found")
        a = self.accounts[self.acct]                              # DEFECT: self-transfer allowed
        if amount <= 0 or amount > a["balance"] - 500: raise ATMError("Insufficient funds")
        a["balance"] -= amount
        self.accounts[to_acct]["balance"] += amount
        self.history.append(f"TRF {amount}")

    def mini_statement(self):
        self._need_login()
        return self.history[-10:]                                 # DEFECT: spec says last 5


# ------------------------------------------------------------------ test harness
def fresh(balance=50000):
    atm = ATM({"1001": {"pin": "1234", "balance": balance}, "1002": {"pin": "4321", "balance": 5000}})
    atm.login("1001", "1234")
    return atm

def verdict(fn):
    try:
        r = fn()
        return "ACCEPTED" if r is None or r is True or isinstance(r, (int, list)) else str(r)
    except ATMError:
        return "REJECTED"

def t_lock():
    atm = ATM({"1001": {"pin": "1234", "balance": 9000}})
    for _ in range(3): verdict(lambda: atm.login("1001", "0000"))
    return verdict(lambda: atm.login("1001", "1234"))   # correct PIN after 3 failures must be rejected

def t_daily():
    atm = fresh(100000)
    for _ in range(2): atm.withdraw(10000)
    return verdict(lambda: atm.withdraw(10000))          # 3rd -> 30,000 in a day

def t_minbal():
    atm = fresh(1000)
    return verdict(lambda: atm.withdraw(600))            # would leave 400 < 500

def t_selftrf():
    atm = fresh(); return verdict(lambda: atm.transfer("1001", 1000))

def t_stmt():
    atm = fresh()
    for _ in range(8): atm.deposit(100)
    return "ACCEPTED" if len(atm.mini_statement()) == 5 else f"WRONG COUNT ({len(atm.mini_statement())})"

def t_logout():
    atm = fresh(); atm.logout(); return verdict(lambda: atm.withdraw(100))

TESTS = [
 # id, spec, description, expected, function, defect meta (None if the test is expected to pass)
 ("T01","S1","Valid login with correct PIN","ACCEPTED", lambda: verdict(lambda: ATM({"1":{"pin":"1234","balance":9}}).login("1","1234")), None),
 ("T02","S1","Account must lock after 3 wrong PINs (correct PIN afterwards rejected)","REJECTED", t_lock,
    ("High","Boundary / Security","Account locks after the 4th wrong PIN instead of the 3rd (off-by-one)")),
 ("T03","S2","Withdraw 500 (valid multiple of 100)","ACCEPTED", lambda: verdict(lambda: fresh().withdraw(500)), None),
 ("T04","S2","Withdraw 550 (not multiple of 100)","REJECTED", lambda: verdict(lambda: fresh().withdraw(550)), None),
 ("T05","S2","Withdraw 0 (below minimum 100)","REJECTED", lambda: verdict(lambda: fresh().withdraw(0)),
    ("Medium","Boundary value","Zero-amount withdrawal is accepted and logged as a transaction")),
 ("T06","S2","Withdraw 10,100 (above per-transaction max)","REJECTED", lambda: verdict(lambda: fresh().withdraw(10100)), None),
 ("T07","S3","Third withdrawal of 10,000 in one day (30,000 > 20,000 daily limit)","REJECTED", t_daily,
    ("Critical","Missing business rule","Daily withdrawal limit is not implemented at all")),
 ("T08","S4","Withdraw 600 from balance 1000 (leaves 400 < min 500)","REJECTED", t_minbal,
    ("High","Business logic","Minimum balance of 500 is not enforced; only 'amount > balance' is checked")),
 ("T09","S5","Deposit 1000","ACCEPTED", lambda: verdict(lambda: fresh().deposit(1000)), None),
 ("T10","S5","Deposit -500 (negative)","REJECTED", lambda: verdict(lambda: fresh().deposit(-500)),
    ("Critical","Input validation","Negative deposit accepted - reduces balance without any withdrawal checks")),
 ("T11","S6","Change PIN to a different 4-digit PIN","ACCEPTED", lambda: verdict(lambda: fresh().change_pin("9999")), None),
 ("T12","S6","Change PIN to same as old PIN","REJECTED", lambda: verdict(lambda: fresh().change_pin("1234")),
    ("Medium","Security","New PIN identical to old PIN is accepted")),
 ("T13","S6","Change PIN to 3-digit / non-numeric value 'ab1'","REJECTED", lambda: verdict(lambda: fresh().change_pin("ab1")),
    ("High","Input validation","PIN format (4 numeric digits) is not validated")),
 ("T14","S7","Transfer 1000 to another valid account","ACCEPTED", lambda: verdict(lambda: fresh().transfer("1002", 1000)), None),
 ("T15","S7","Transfer to the SAME account","REJECTED", t_selftrf,
    ("Medium","Business logic","Self-transfer is allowed and creates a meaningless transaction")),
 ("T16","S7","Transfer to non-existent account 9999","REJECTED", lambda: verdict(lambda: fresh().transfer("9999", 100)), None),
 ("T17","S8","Mini statement must contain only last 5 transactions","ACCEPTED", t_stmt,
    ("Low","Functional","Mini statement returns last 10 entries instead of 5")),
 ("T18","S9","Withdraw after logout","REJECTED", t_logout,
    ("Critical","Security / Session","Session not cleared on logout - next person can withdraw money")),
 ("T19","-","Withdraw 5000 from balance 1000 (insufficient funds)","REJECTED", lambda: verdict(lambda: fresh(1000).withdraw(5000)), None),
]

def main():
    print(SPEC)
    print("=" * 100); print("TEST EXECUTION LOG"); print("=" * 100)
    print(f"{'ID':<5}{'Spec':<6}{'Description':<72}{'Expected':<10}{'Actual':<10}Result")
    defects = []
    for tid, spec, desc, exp, fn, meta in TESTS:
        act = fn()
        res = "PASS" if act == exp else "FAIL"
        print(f"{tid:<5}{spec:<6}{desc[:70]:<72}{exp:<10}{act:<10}{res}")
        if res == "FAIL":
            defects.append((tid, spec, desc, exp, act, meta))
    print("\n" + "=" * 100); print("DEFECT REPORT"); print("=" * 100)
    for i, (tid, spec, desc, exp, act, meta) in enumerate(defects, 1):
        sev, cat, text = meta
        print(f"Defect ID   : DEF-{i:03d}")
        print(f"Found by    : {tid} (violates specification clause {spec})")
        print(f"Severity    : {sev}")
        print(f"Category    : {cat}")
        print(f"Description : {text}")
        print(f"Test step   : {desc}")
        print(f"Expected    : {exp}    Actual: {act}")
        print("-" * 100)
    sev_count = {}
    for d in defects: sev_count[d[5][0]] = sev_count.get(d[5][0], 0) + 1
    print(f"Tests executed : {len(TESTS)}   Passed: {len(TESTS)-len(defects)}   Failed (defects): {len(defects)}")
    print("Defects by severity :", ", ".join(f"{k}={v}" for k, v in sorted(sev_count.items())))

if __name__ == "__main__":
    main()
