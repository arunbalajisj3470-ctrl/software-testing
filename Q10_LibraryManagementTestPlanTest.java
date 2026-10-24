import java.time.LocalDate;
import java.util.List;

/**
 * Q10. Test Plan - Library Management System.
 * Executable version of the sample test cases (LMS-TC-01 .. LMS-TC-05) from the Test Plan,
 * plus the boundary-value and equivalence-partition cases named in its "Test Techniques" section.
 * Run:  javac *.java  &&  java Q10_LibraryManagementTestPlanTest
 */
public class Q10_LibraryManagementTestPlanTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    static void expectError(String id, String description, String expectedText, Runnable action) {
        try {
            action.run();
            check(id, description + " (expected an error, none thrown)", false);
        } catch (LibraryApp.AppException e) {
            check(id, description + " -> \"" + e.getMessage() + "\"",
                    e.getMessage().toLowerCase().contains(expectedText.toLowerCase()));
        }
    }

    static LibraryApp library() {
        LibraryApp lib = new LibraryApp();
        lib.addUser("lib1", "pw", "LIBRARIAN");
        lib.addUser("stu1", "pw", "MEMBER");
        for (int i = 1; i <= 5; i++)
            lib.addBook("lib1", "ISBN" + i, "Book " + i, "Author " + i, "Tech", 2);
        lib.addBook("lib1", "ISBN9", "Java Programming", "James Gosling", "Tech", 1);
        lib.addMember("M1", "Arun", LocalDate.of(2027, 12, 31));
        lib.addMember("M2", "Priya", LocalDate.of(2026, 1, 1));   // expired membership
        return lib;
    }

    public static void main(String[] args) {

        // LMS-TC-01 Issue book to valid member
        final LibraryApp lib = library();
        int before = lib.book("ISBN1").available;
        LibraryApp.Loan loan = lib.issue("M1", "ISBN1");
        check("LMS-TC-01a", "Issue succeeds; stock reduced by 1", lib.book("ISBN1").available == before - 1);
        check("LMS-TC-01b", "Due date = today + 14 days", loan.due.equals(LocalDate.of(2026, 10, 20).plusDays(14)));

        // LMS-TC-02 / BVA on issue limit (2, 3, 4 books)
        lib.issue("M1", "ISBN2");
        check("BVA-ISSUE-2", "2 books issued (below limit) - allowed", lib.activeLoans("M1") == 2);
        lib.issue("M1", "ISBN3");
        check("BVA-ISSUE-3", "3rd book issued (at limit) - allowed", lib.activeLoans("M1") == 3);
        expectError("LMS-TC-02", "4th book blocked (above limit)", "issue limit reached",
                () -> lib.issue("M1", "ISBN4"));

        // LMS-TC-03 Return overdue book -> fine
        lib.setToday(LocalDate.of(2026, 10, 20).plusDays(14 + 5));
        double fine = lib.returnBook("M1", "ISBN1");
        check("LMS-TC-03", "Book returned 5 days late -> fine Rs.10", fine == 10.0 && lib.member("M1").fine == 10.0);
        expectError("DT-FINE", "Member with pending fine cannot borrow", "pending fine",
                () -> lib.issue("M1", "ISBN4"));
        lib.payFine("M1", 10.0);
        lib.issue("M1", "ISBN4");
        check("DT-FINE-2", "After paying the fine the member can borrow again", lib.activeLoans("M1") == 3);

        // LMS-TC-04 Search by ISBN / title / author
        List<LibraryApp.Book> byIsbn = lib.search("ISBN9");
        check("LMS-TC-04a", "Search by valid ISBN returns the correct book",
                byIsbn.size() == 1 && byIsbn.get(0).title.equals("Java Programming"));
        check("LMS-TC-04b", "Search by author fragment works", lib.search("gosling").size() == 1);
        check("LMS-TC-04c", "Search with no match returns empty list", lib.search("zzzz").isEmpty());

        // LMS-TC-05 Login with wrong password
        expectError("LMS-TC-05", "Wrong password denied", "invalid credentials", () -> lib.login("lib1", "bad"));
        check("LMS-TC-05b", "Correct login returns the role", "LIBRARIAN".equals(lib.login("lib1", "pw")));

        // Equivalence partitioning of member status: valid / expired / blocked
        final LibraryApp lib2 = library();
        expectError("EP-EXPIRED", "Expired member cannot borrow", "membership expired", () -> lib2.issue("M2", "ISBN1"));
        lib2.member("M1").blocked = true;
        expectError("EP-BLOCKED", "Blocked member cannot borrow", "blocked", () -> lib2.issue("M1", "ISBN1"));

        // Availability, reservation, notification
        final LibraryApp lib3 = library();
        lib3.addMember("M3", "Kiran", LocalDate.of(2027, 12, 31));
        lib3.issue("M1", "ISBN9");
        expectError("RES-1", "Issue of a fully issued book is refused", "not available",
                () -> lib3.issue("M3", "ISBN9"));
        lib3.reserve("M3", "ISBN9");
        lib3.returnBook("M1", "ISBN9");
        check("RES-2", "Reserving member is notified when the book is returned",
                lib3.notifications().contains("Book ISBN9 is now available for M3"));

        // Renewal limit (max 2)
        final LibraryApp lib4 = library();
        lib4.issue("M1", "ISBN1");
        lib4.renew("M1", "ISBN1");
        lib4.renew("M1", "ISBN1");
        expectError("RENEW-3", "Third renewal refused", "renewal limit", () -> lib4.renew("M1", "ISBN1"));

        // Role-based access, duplicate ISBN, delete issued book
        expectError("SEC-1", "Member role cannot add books", "access denied",
                () -> lib4.addBook("stu1", "ISBN77", "T", "A", "Tech", 1));
        expectError("DUP-1", "Duplicate ISBN rejected", "already exists",
                () -> lib4.addBook("lib1", "ISBN1", "T", "A", "Tech", 1));
        expectError("DEL-1", "Cannot delete a book that is currently issued", "currently issued",
                () -> lib4.deleteBook("lib1", "ISBN1"));

        // Overdue report
        lib4.setToday(LocalDate.of(2026, 10, 20).plusDays(50));   // due date after 2 renewals is day 42
        check("REP-1", "Overdue report lists the unreturned overdue loan", lib4.overdueReport("lib1").size() == 1);
        expectError("SEC-2", "Member cannot open reports", "access denied", () -> lib4.overdueReport("stu1"));

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
