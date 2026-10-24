import java.time.LocalDate;
import java.util.*;

/**
 * Simple in-memory model of a Net Banking application.
 * Used by Q08 (positive) and Q09 (negative) test programs.
 */
public class BankingApp {

    public static class AppException extends RuntimeException {
        public AppException(String message) { super(message); }
    }

    public static class Account {
        public final String number;
        public double balance;
        public boolean frozen;
        public final String cardLast4;
        public Account(String number, double balance, String cardLast4) {
            this.number = number; this.balance = balance; this.cardLast4 = cardLast4;
        }
    }

    public static class Txn {
        public final String ref, type, account;
        public final double amount;
        public final LocalDate date;
        Txn(String ref, String type, String account, double amount, LocalDate date) {
            this.ref = ref; this.type = type; this.account = account; this.amount = amount; this.date = date;
        }
    }

    private static class Customer {
        String userId, password, accountNo;
        int failed;
        boolean locked;
    }

    public static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000L;
    public static final double DAILY_LIMIT = 100000;
    private static final String OTP = "654321";

    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, Map<String, String>> beneficiaries = new HashMap<>();
    private final Map<String, String> processed = new HashMap<>();
    private final List<Txn> txns = new ArrayList<>();
    private final List<String> alerts = new ArrayList<>();
    private final Set<String> blockedCards = new HashSet<>();
    private String sessionUser;
    private long clock, lastActivity;
    private boolean otpActive, networkDown;
    private double dailyTotal;
    private int refCounter = 1;
    private LocalDate today = LocalDate.of(2026, 10, 20);

    // ---------- Setup helpers (bank back-office) ----------
    public void openAccount(String number, double balance, String cardLast4) {
        accounts.put(number, new Account(number, balance, cardLast4));
    }
    public void setFrozen(String number, boolean frozen) { accounts.get(number).frozen = frozen; }
    public double balanceOf(String number) { return accounts.get(number).balance; }
    public void setToday(LocalDate d) { today = d; }
    public void setNetworkDown(boolean down) { networkDown = down; }
    public void advanceClock(long ms) { clock += ms; }

    private static boolean strongPassword(String p) {
        return p != null && p.length() >= 8 && p.matches(".*[A-Z].*") && p.matches(".*[a-z].*")
                && p.matches(".*\\d.*") && p.matches(".*[^A-Za-z0-9].*");
    }

    // ---------- OTP ----------
    public String generateOtp() { otpActive = true; return OTP; }
    public void expireOtp() { otpActive = false; }
    private void verifyOtp(String otp) {
        if (!otpActive) throw new AppException("OTP expired");
        if (!OTP.equals(otp)) throw new AppException("Invalid OTP");
    }

    // ---------- Registration, login, session ----------
    public void registerNetBanking(String accountNo, String cardLast4, String otp, String userId, String password) {
        Account a = accounts.get(accountNo);
        if (a == null || !a.cardLast4.equals(cardLast4)) throw new AppException("Invalid account or card details");
        verifyOtp(otp);
        if (!strongPassword(password)) throw new AppException("Password does not meet policy");
        Customer c = new Customer();
        c.userId = userId; c.password = password; c.accountNo = accountNo;
        customers.put(userId, c);
    }

    public void login(String userId, String password, String otp) {
        if (userId == null || userId.isEmpty() || password == null || password.isEmpty())
            throw new AppException("Required field");
        Customer c = customers.get(userId);
        if (c == null) throw new AppException("Invalid user ID or password");
        if (c.locked) throw new AppException("Account locked. Contact the bank");
        if (!c.password.equals(password)) {
            c.failed++;
            if (c.failed >= 3) {
                c.locked = true;
                alerts.add("ALERT: account " + c.accountNo + " locked after failed login attempts");
                throw new AppException("Account locked. Contact the bank");
            }
            throw new AppException("Invalid user ID or password");
        }
        verifyOtp(otp);
        c.failed = 0;
        sessionUser = userId;
        lastActivity = clock;
    }

    public void logout() { sessionUser = null; }

    private Customer session() {
        if (sessionUser == null) throw new AppException("Please login");
        if (clock - lastActivity > SESSION_TIMEOUT_MS) { sessionUser = null; throw new AppException("Session expired"); }
        lastActivity = clock;
        return customers.get(sessionUser);
    }

    public boolean isLoggedIn() { return sessionUser != null; }

    // ---------- Account information ----------
    public double balance() { return accounts.get(session().accountNo).balance; }

    public List<Txn> miniStatement(int n) {
        String acc = session().accountNo;
        List<Txn> mine = new ArrayList<>();
        for (Txn t : txns) if (t.account.equals(acc)) mine.add(t);
        return mine.subList(Math.max(0, mine.size() - n), mine.size());
    }

    public String statement(LocalDate from, LocalDate to) {
        String acc = session().accountNo;
        if (from.isAfter(to)) throw new AppException("From date cannot be after To date");
        StringBuilder sb = new StringBuilder("STATEMENT " + acc + " " + from + " to " + to + "\n");
        for (Txn t : txns)
            if (t.account.equals(acc) && !t.date.isBefore(from) && !t.date.isAfter(to))
                sb.append(t.date).append(' ').append(t.type).append(' ').append(t.amount).append(' ').append(t.ref).append('\n');
        return sb.toString();
    }

    // ---------- Beneficiaries ----------
    public void addBeneficiary(String name, String accountNo, String ifsc, String otp) {
        String user = session().userId;
        if (name == null || name.trim().isEmpty()) throw new AppException("Name is required");
        if (name.contains("<") || name.contains(">")) throw new AppException("Invalid characters in name");
        if (accountNo == null || !accountNo.matches("\\d{9,18}")) throw new AppException("Invalid account number");
        if (ifsc == null || !ifsc.matches("[A-Z]{4}0[A-Z0-9]{6}")) throw new AppException("Invalid IFSC code");
        Map<String, String> map = beneficiaries.computeIfAbsent(user, k -> new HashMap<>());
        if (map.containsKey(accountNo)) throw new AppException("Beneficiary already exists");
        verifyOtp(otp);
        map.put(accountNo, ifsc);
    }

    public boolean hasBeneficiary(String accountNo) {
        Map<String, String> m = beneficiaries.get(sessionUser);
        return m != null && m.containsKey(accountNo);
    }

    // ---------- Fund transfer ----------
    /** Idempotent: a repeated requestId returns the original reference without a second debit. */
    public String transfer(String requestId, String toAccount, String amountText, String otp, String mode) {
        Customer c = session();
        if (processed.containsKey(requestId)) return processed.get(requestId);
        double amount;
        try { amount = Double.parseDouble(amountText); }
        catch (Exception e) { throw new AppException("Enter a valid amount"); }
        if (Double.isNaN(amount) || amount <= 0) throw new AppException("Enter a valid amount");
        if (!hasBeneficiary(toAccount)) throw new AppException("Beneficiary not registered");
        Account from = accounts.get(c.accountNo);
        Account to = accounts.get(toAccount);
        if (to != null && to.frozen) throw new AppException("Transaction failed: beneficiary account is frozen");
        if (amount > from.balance) throw new AppException("Insufficient balance");
        if (dailyTotal + amount > DAILY_LIMIT) throw new AppException("Daily transaction limit exceeded");
        verifyOtp(otp);
        if (networkDown) throw new AppException("Network error. Transaction not processed");

        from.balance -= amount;
        if (to != null) to.balance += amount;
        dailyTotal += amount;
        String ref = mode + "-REF" + (refCounter++);
        txns.add(new Txn(ref, "DEBIT", from.number, amount, today));
        if (to != null) txns.add(new Txn(ref, "CREDIT", to.number, amount, today));
        alerts.add("SMS: Rs." + amount + " debited from " + from.number + ". Bal Rs." + from.balance + ". Ref " + ref);
        alerts.add("EMAIL: Rs." + amount + " debited from " + from.number + ". Ref " + ref);
        processed.put(requestId, ref);
        return ref;
    }

    // ---------- Bills, deposits, card, password ----------
    public String payBill(String type, String consumerNo, double amount) {
        Customer c = session();
        if (consumerNo == null || !consumerNo.matches("\\d{10}")) throw new AppException("Invalid consumer number");
        Account a = accounts.get(c.accountNo);
        if (amount <= 0 || amount > a.balance) throw new AppException("Insufficient balance or invalid amount");
        a.balance -= amount;
        String ref = "RCPT" + (refCounter++);
        txns.add(new Txn(ref, "BILL-" + type, a.number, amount, today));
        return ref;
    }

    /** Returns maturity amount (simple 7% p.a.). */
    public double createFixedDeposit(double amount, int years) {
        Account a = accounts.get(session().accountNo);
        if (amount < 1000 || amount > a.balance) throw new AppException("Invalid FD amount");
        if (years < 1) throw new AppException("Invalid tenure");
        a.balance -= amount;
        return amount * (1 + 0.07 * years);
    }

    public void blockCard(String last4) {
        Account a = accounts.get(session().accountNo);
        if (!a.cardLast4.equals(last4)) throw new AppException("Card not found");
        blockedCards.add(last4);
        alerts.add("SMS: Card ending " + last4 + " blocked");
    }
    public boolean isCardBlocked(String last4) { return blockedCards.contains(last4); }

    public void changePassword(String oldPwd, String newPwd, String confirm) {
        Customer c = session();
        if (!c.password.equals(oldPwd)) throw new AppException("Old password incorrect");
        if (!newPwd.equals(confirm)) throw new AppException("Passwords do not match");
        if (!strongPassword(newPwd)) throw new AppException("Password does not meet policy");
        c.password = newPwd;
    }

    public List<String> alerts() { return alerts; }
}
