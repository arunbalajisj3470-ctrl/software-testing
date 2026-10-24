/**
 * Q9. Banking Application - NEGATIVE test cases (TC-N01 .. TC-N22)
 * Run:  javac *.java  &&  java Q09_BankingNegativeTest
 */
public class Q09_BankingNegativeTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    static void expectError(String id, String description, String expectedText, Runnable action) {
        try {
            action.run();
            check(id, description + " (expected an error, none thrown)", false);
        } catch (BankingApp.AppException e) {
            check(id, description + " -> \"" + e.getMessage() + "\"",
                    e.getMessage().toLowerCase().contains(expectedText.toLowerCase()));
        }
    }

    static BankingApp freshBank() {
        BankingApp b = new BankingApp();
        b.openAccount("111122223333", 500000, "1234");
        b.openAccount("444455556666", 1000, "5678");
        b.registerNetBanking("111122223333", "1234", b.generateOtp(), "arun01", "Strong@123");
        return b;
    }

    static BankingApp loggedInBank() {
        BankingApp b = freshBank();
        b.login("arun01", "Strong@123", b.generateOtp());
        b.addBeneficiary("Ravi", "444455556666", "HDFC0001234", b.generateOtp());
        return b;
    }

    public static void main(String[] args) {

        // ---------- Login ----------
        final BankingApp b1 = freshBank();
        expectError("TC-N01", "Wrong password rejected without revealing which field is wrong", "invalid user id or password",
                () -> b1.login("arun01", "wrong", b1.generateOtp()));
        expectError("TC-N02", "Blank user ID and password rejected", "required field",
                () -> b1.login("", "", "000000"));
        try { b1.login("arun01", "wrong", b1.generateOtp()); } catch (BankingApp.AppException ignored) { }
        expectError("TC-N03", "Account locked after 3 wrong passwords", "locked",
                () -> b1.login("arun01", "wrong", b1.generateOtp()));
        expectError("TC-N03b", "Locked account cannot login even with the correct password", "locked",
                () -> b1.login("arun01", "Strong@123", b1.generateOtp()));

        final BankingApp b2 = freshBank();
        b2.generateOtp();
        expectError("TC-N04", "Wrong OTP at login rejected", "invalid otp",
                () -> b2.login("arun01", "Strong@123", "123456"));
        b2.generateOtp();
        b2.expireOtp();
        expectError("TC-N05", "Expired OTP at login rejected", "expired",
                () -> b2.login("arun01", "Strong@123", "654321"));
        check("TC-N04b", "User is not logged in after OTP failures", !b2.isLoggedIn());

        // ---------- Fund transfer ----------
        final BankingApp b3 = loggedInBank();
        // Make the balance low for the insufficient-balance case
        final BankingApp low = new BankingApp();
        low.openAccount("111122223333", 2000, "1234");
        low.openAccount("444455556666", 1000, "5678");
        low.registerNetBanking("111122223333", "1234", low.generateOtp(), "arun01", "Strong@123");
        low.login("arun01", "Strong@123", low.generateOtp());
        low.addBeneficiary("Ravi", "444455556666", "HDFC0001234", low.generateOtp());
        expectError("TC-N06", "Transfer above balance rejected", "insufficient balance",
                () -> low.transfer("R1", "444455556666", "5000", low.generateOtp(), "IMPS"));
        check("TC-N06b", "No money debited on insufficient-balance failure", low.balanceOf("111122223333") == 2000.0);

        expectError("TC-N07", "Transfer above daily limit rejected", "daily transaction limit",
                () -> b3.transfer("R2", "444455556666", "150000", b3.generateOtp(), "NEFT"));
        check("TC-N07b", "Balance unchanged after limit failure", b3.balanceOf("111122223333") == 500000.0);

        expectError("TC-N08", "Zero amount rejected", "valid amount",
                () -> b3.transfer("R3", "444455556666", "0", b3.generateOtp(), "IMPS"));
        expectError("TC-N09", "Negative amount rejected", "valid amount",
                () -> b3.transfer("R4", "444455556666", "-500", b3.generateOtp(), "IMPS"));
        expectError("TC-N10", "Alphabetic amount rejected", "valid amount",
                () -> b3.transfer("R5", "444455556666", "abc", b3.generateOtp(), "IMPS"));
        check("TC-N09b", "Receiver balance unchanged after invalid amounts", b3.balanceOf("444455556666") == 1000.0);

        expectError("TC-N04c", "Wrong OTP on transfer rejected", "invalid otp",
                () -> b3.transfer("R6", "444455556666", "100", "111111", "IMPS"));

        // ---------- Beneficiary ----------
        expectError("TC-N11", "Invalid IFSC rejected", "invalid ifsc",
                () -> b3.addBeneficiary("Sam", "123456789012", "ABCD123", b3.generateOtp()));
        expectError("TC-N12", "Short account number rejected", "invalid account number",
                () -> b3.addBeneficiary("Sam", "12345", "HDFC0001234", b3.generateOtp()));
        expectError("TC-N13", "Duplicate beneficiary rejected", "already exists",
                () -> b3.addBeneficiary("Ravi", "444455556666", "HDFC0001234", b3.generateOtp()));
        expectError("TC-N22", "Script tag in beneficiary name rejected", "invalid characters",
                () -> b3.addBeneficiary("<script>alert(1)</script>", "777788889999", "HDFC0001234", b3.generateOtp()));

        // ---------- Frozen account ----------
        b3.setFrozen("444455556666", true);
        expectError("TC-N14", "Transfer to a frozen account fails", "frozen",
                () -> b3.transfer("R7", "444455556666", "1000", b3.generateOtp(), "IMPS"));
        check("TC-N14b", "Sender not debited for frozen-account transfer", b3.balanceOf("111122223333") == 500000.0);
        b3.setFrozen("444455556666", false);

        // ---------- Network failure and double submit ----------
        b3.setNetworkDown(true);
        expectError("TC-N15", "Network failure: transaction not processed", "network error",
                () -> b3.transfer("R8", "444455556666", "2000", b3.generateOtp(), "IMPS"));
        check("TC-N15b", "No debit on network failure", b3.balanceOf("111122223333") == 500000.0);
        b3.setNetworkDown(false);
        String ref1 = b3.transfer("R8", "444455556666", "2000", b3.generateOtp(), "IMPS");
        String ref2 = b3.transfer("R8", "444455556666", "2000", b3.generateOtp(), "IMPS");   // double click / refresh
        check("TC-N16", "Same request submitted twice is processed once",
                ref1.equals(ref2) && b3.balanceOf("111122223333") == 498000.0);

        // ---------- Change password ----------
        expectError("TC-N17", "Mismatched new passwords rejected", "do not match",
                () -> b3.changePassword("Strong@123", "NewPass@123", "Different@123"));
        expectError("TC-N18", "Weak password rejected", "policy",
                () -> b3.changePassword("Strong@123", "12345", "12345"));
        expectError("TC-N17b", "Wrong old password rejected", "old password incorrect",
                () -> b3.changePassword("WrongOld@1", "NewPass@123", "NewPass@123"));

        // ---------- Session / access control ----------
        b3.logout();
        expectError("TC-N19", "Dashboard not accessible after logout / direct URL", "please login",
                () -> b3.balance());

        // ---------- Injection and tampering ----------
        final BankingApp b4 = freshBank();
        try {
            b4.login("admin' --", "x", b4.generateOtp());
            check("TC-N20", "SQL injection login must fail", false);
        } catch (BankingApp.AppException e) {
            check("TC-N20", "SQL injection text rejected as ordinary invalid credentials, no DB error shown",
                    e.getMessage().equals("Invalid user ID or password") && !e.getMessage().toLowerCase().contains("sql"));
        }
        final BankingApp b5 = loggedInBank();
        expectError("TC-N21", "Tampered request to an unregistered account rejected by server validation", "beneficiary not registered",
                () -> b5.transfer("R9", "000011112222", "100", b5.generateOtp(), "IMPS"));

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
