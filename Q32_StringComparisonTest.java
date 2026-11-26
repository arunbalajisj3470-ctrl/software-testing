// Q32: White-box test case (JUnit) for string comparison, validated with assert statements.
// White-box technique: tests are derived from the code's branches (null checks, equals vs ==, case handling).

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Q32_StringComparisonTest {

    // Unit under test: compares two strings, with branches we want to cover
    static boolean compareStrings(String a, String b, boolean ignoreCase) {
        if (a == null || b == null) {          // Branch 1: null handling
            return a == b;
        }
        if (ignoreCase) {                      // Branch 2: case-insensitive comparison
            return a.equalsIgnoreCase(b);
        }
        return a.equals(b);                    // Branch 3: exact comparison
    }

    @Test
    void branchNullInputs() {
        assertTrue(compareStrings(null, null, false));
        assertFalse(compareStrings("abc", null, false));
        assertFalse(compareStrings(null, "abc", true));
    }

    @Test
    void branchExactComparison() {
        assertTrue(compareStrings("Java", "Java", false));
        assertFalse(compareStrings("Java", "java", false));
    }

    @Test
    void branchIgnoreCase() {
        assertTrue(compareStrings("Java", "JAVA", true));
        assertFalse(compareStrings("Java", "Jav", true));
    }

    @Test
    void sameReferenceAndEqualContentAreBothEqual() {
        String s1 = "test";
        String s2 = new String("test");
        assertNotSame(s1, s2);                 // different objects in memory
        assertTrue(compareStrings(s1, s2, false));  // but equal by value
    }

    public static void main(String[] args) {
        System.out.println(compareStrings("Java", "java", true));  // true
    }
}
