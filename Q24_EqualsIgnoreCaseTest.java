import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q24: JUnit program to check whether two strings are equal ignoring case.
 *
 * White-box analysis of equalsIgnoreCase():
 *   Branch 1: both references null         -> true
 *   Branch 2: exactly one is null          -> false
 *   Branch 3: lengths differ               -> false
 *   Loop    : compares char by char (lowercased)
 *             - mismatch found -> false
 *             - all match      -> true
 *   Tests cover every branch and loop outcome.
 */
class StringComparator {

    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        for (int i = 0; i < a.length(); i++) {
            if (Character.toLowerCase(a.charAt(i)) != Character.toLowerCase(b.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(equalsIgnoreCase("Hello", "hELLO"));   // true
        System.out.println(equalsIgnoreCase("Java", "Python"));   // false
    }
}

public class Q24_EqualsIgnoreCaseTest {

    @Test
    void testSameStringDifferentCase() {
        assertTrue(StringComparator.equalsIgnoreCase("Hello", "hELLO"));
    }

    @Test
    void testIdenticalStrings() {
        assertTrue(StringComparator.equalsIgnoreCase("Java", "Java"));
    }

    @Test
    void testDifferentStringsSameLength() {
        assertFalse(StringComparator.equalsIgnoreCase("Java", "Lava"));
    }

    @Test
    void testDifferentLengths() {
        assertFalse(StringComparator.equalsIgnoreCase("abc", "abcd"));
    }

    @Test
    void testBothNull() {
        assertTrue(StringComparator.equalsIgnoreCase(null, null));
    }

    @Test
    void testOneNull() {
        assertFalse(StringComparator.equalsIgnoreCase(null, "a"));
        assertFalse(StringComparator.equalsIgnoreCase("a", null));
    }

    @Test
    void testEmptyStrings() {
        assertTrue(StringComparator.equalsIgnoreCase("", ""));
    }

    @Test
    void testWithDigitsAndSymbols() {
        assertTrue(StringComparator.equalsIgnoreCase("Test#123", "tEST#123"));
        assertFalse(StringComparator.equalsIgnoreCase("Test#123", "Test#124"));
    }

    @Test
    void testResultMatchesJavaBuiltIn() {
        String a = "OpenAI", b = "openai";
        assertEquals(a.equalsIgnoreCase(b), StringComparator.equalsIgnoreCase(a, b));
    }
}
