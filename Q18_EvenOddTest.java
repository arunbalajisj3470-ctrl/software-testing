import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q18: JUnit test to check whether a number is even or odd (white-box testing).
 *
 * White-box analysis of isEven():
 *   Statements : 1 decision (n % 2 == 0), 2 return statements
 *   Branches   : TRUE branch (even) and FALSE branch (odd)
 *   Paths      : 2 independent paths -> cyclomatic complexity = 2
 *   Tests below cover 100% statement, branch and path coverage.
 *
 * Run (JUnit 5 console launcher or any IDE):
 *   javac -cp junit-platform-console-standalone.jar Q18_EvenOddTest.java
 *   java -jar junit-platform-console-standalone.jar -cp . --scan-classpath
 */
class NumberChecker {
    public static boolean isEven(int n) {
        if (n % 2 == 0) {      // decision point
            return true;       // path 1
        } else {
            return false;      // path 2
        }
    }

    public static String check(int n) {
        return isEven(n) ? "Even" : "Odd";
    }

    public static void main(String[] args) {
        int[] samples = {10, 7, 0, -4, -9};
        for (int s : samples) {
            System.out.println(s + " is " + check(s));
        }
    }
}

public class Q18_EvenOddTest {

    @Test
    void testEvenNumber_TrueBranch() {
        assertTrue(NumberChecker.isEven(8));
        assertEquals("Even", NumberChecker.check(8));
    }

    @Test
    void testOddNumber_FalseBranch() {
        assertFalse(NumberChecker.isEven(7));
        assertEquals("Odd", NumberChecker.check(7));
    }

    @Test
    void testZeroIsEven_BoundaryValue() {
        assertTrue(NumberChecker.isEven(0));
    }

    @Test
    void testNegativeEven() {
        assertTrue(NumberChecker.isEven(-4));
    }

    @Test
    void testNegativeOdd() {
        assertFalse(NumberChecker.isEven(-9));
    }

    @Test
    void testLargeValues() {
        assertTrue(NumberChecker.isEven(Integer.MAX_VALUE - 1));
        assertFalse(NumberChecker.isEven(Integer.MAX_VALUE));
    }
}
