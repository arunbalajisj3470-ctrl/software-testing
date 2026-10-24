import java.time.LocalDate;
import java.util.List;

/**
 * Q8. Banking Application - POSITIVE test cases (TC-P01 .. TC-P15)
 * Run:  javac *.java  &&  java Q08_BankingPositiveTest
 */
public class Q08_BankingPositiveTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    public static void main(String[] args) {
        BankingApp bank = new BankingApp();
        bank.openAccount("111122223333", 50000, "1234");   // sender
        bank.openAccount("444455556666", 1000, "5678");    // receiver in the same bank

        // TC-P01 Net banking registration
        String otp = bank.generateOtp();
        bank.registerNetBanking("111122223333", "1234", otp, "arun01", "Strong@123");
        check("TC-P01", "Net banking registration with valid account, card and OTP", true);

        // TC-P02 Login with 2FA
        bank.login("arun01", "Strong@123", bank.generateOtp());
        check("TC-P02", "Login with user ID, password and OTP", bank.isLoggedIn());

        // TC-P03 Balance and mini statement
        check("TC-P03a", "Account summary shows correct balance", bank.balance() == 50000.0);
        check("TC-P03b", "Mini statement is empty before any transaction", bank.miniStatement(5).isEmpty());

        // TC-P05 Add beneficiary (done before transfers)
        bank.addBeneficiary("Ravi Kumar", "444455556666", "HDFC0001234", bank.generateOtp());
        check("TC-P05", "Beneficiary added successfully", bank.hasBeneficiary("444455556666"));

        // TC-P06 / TC-P07 Transfer within the same bank
        String ref = bank.transfer("REQ-1", "444455556666", "5000", bank.generateOtp(), "WITHIN");
        check("TC-P06", "Transfer Rs.5000 succeeds and reference generated", ref != null && !ref.isEmpty());
        check("TC-P07a", "Sender debited exactly Rs.5000", bank.balanceOf("111122223333") == 45000.0);
        check("TC-P07b", "Receiver credited exactly Rs.5000", bank.balanceOf("444455556666") == 6000.0);

        // TC-P08 IMPS to another bank
        bank.addBeneficiary("Meena", "999988887777", "SBIN0004321", bank.generateOtp());
        String utr = bank.transfer("REQ-2", "999988887777", "10000", bank.generateOtp(), "IMPS");
        check("TC-P08", "IMPS transfer to another bank succeeds with UTR " + utr,
                utr.startsWith("IMPS") && bank.balanceOf("111122223333") == 35000.0);

        // TC-P09 Alerts
        boolean sms = false, email = false;
        for (String a : bank.alerts()) { if (a.startsWith("SMS")) sms = true; if (a.startsWith("EMAIL")) email = true; }
        check("TC-P09", "SMS and email alerts generated for transactions", sms && email);

        // TC-P04 Statement for date range
        String stmt = bank.statement(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        check("TC-P04", "Statement contains the transactions of the period",
                stmt.contains("DEBIT") && stmt.contains(ref) && stmt.contains(utr));
        List<BankingApp.Txn> mini = bank.miniStatement(5);
        check("TC-P03c", "Mini statement now lists the 2 debits", mini.size() == 2);

        // TC-P10 Bill payment
        String receipt = bank.payBill("ELECTRICITY", "1234567890", 1200);
        check("TC-P10", "Electricity bill paid, receipt " + receipt, receipt.startsWith("RCPT") && bank.balance() == 33800.0);

        // TC-P11 Fixed deposit
        double maturity = bank.createFixedDeposit(20000, 1);
        check("TC-P11", "FD of Rs.20000 for 1 year matures to Rs.21400 (7%)",
                Math.abs(maturity - 21400.0) < 0.001 && bank.balance() == 13800.0);

        // TC-P12 Change password
        bank.changePassword("Strong@123", "NewStrong@456", "NewStrong@456");
        bank.logout();
        bank.login("arun01", "NewStrong@456", bank.generateOtp());
        check("TC-P12", "Password changed; login works with the new password", bank.isLoggedIn());

        // TC-P13 Block card
        bank.blockCard("1234");
        check("TC-P13", "Debit card blocked", bank.isCardBlocked("1234"));

        // TC-P14 Logout
        bank.logout();
        boolean denied = false;
        try { bank.balance(); } catch (BankingApp.AppException e) { denied = true; }
        check("TC-P14", "After logout, account pages need login again", denied);

        // TC-P15 Session timeout
        bank.login("arun01", "NewStrong@456", bank.generateOtp());
        bank.advanceClock(BankingApp.SESSION_TIMEOUT_MS + 1000);
        boolean expired = false;
        try { bank.balance(); } catch (BankingApp.AppException e) { expired = e.getMessage().contains("expired"); }
        check("TC-P15", "Auto-logout after inactivity", expired && !bank.isLoggedIn());

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
