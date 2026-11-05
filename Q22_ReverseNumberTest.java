import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q22: JUnit test case to reverse a number, validated with white-box testing.
 *
 * White-box analysis of reverse():
 *   Branch 1: n < 0  -> remember sign, work on absolute value
 *   Loop     : while (n != 0)  -> 0 iterations (n = 0), 1 iteration (single digit), many iterations
 *   Trailing zeros are dropped naturally (100 -> 1)
 *   Tests cover each branch and each loop scenario.
 */
class NumberReverser {

    public static long reverse(long n) {
        boolean negative = n < 0;
        long num = Math.abs(n);
        long reversed = 0;
        while (num != 0) {
            long digit = num % 10;
            reversed = reversed * 10 + digit;
            num /= 10;
        }
        return negative ? -reversed : reversed;
    }

    public static void main(String[] args) {
        long[] samples = {1234, 100, 0, -123, 7};
        for (long s : samples) {
            System.out.println(s + " reversed = " + reverse(s));
        }
    }
}

public class Q22_ReverseNumberTest {

    @Test
    void testMultiDigitNumber() {
        assertEquals(4321, NumberReverser.reverse(1234));
    }

    @Test
    void testSingleDigit_OneLoopIteration() {
        assertEquals(7, NumberReverser.reverse(7));
    }

    @Test
    void testZero_LoopNotExecuted() {
        assertEquals(0, NumberReverser.reverse(0));
    }

    @Test
    void testTrailingZerosDropped() {
        assertEquals(1, NumberReverser.reverse(100));
        assertEquals(21, NumberReverser.reverse(1200));
    }

    @Test
    void testNegativeNumber_NegativeBranch() {
        assertEquals(-321, NumberReverser.reverse(-123));
    }

    @Test
    void testPalindromeNumber() {
        assertEquals(12321, NumberReverser.reverse(12321));
    }

    @Test
    void testReverseTwiceRestoresOriginal() {
        long n = 98765;
        assertEquals(n, NumberReverser.reverse(NumberReverser.reverse(n)));
    }
}
