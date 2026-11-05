import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q23: Sum of digits of a number, verified with white-box testing.
 *
 * White-box analysis of sumOfDigits():
 *   Statement 1: Math.abs(n) handles negative input
 *   Loop       : while (n > 0) -> 0 iterations (n = 0), 1 iteration, many iterations
 *   Tests cover statement, branch and loop coverage (zero / one / many iterations).
 */
class DigitSum {

    public static int sumOfDigits(int n) {
        n = Math.abs(n);
        int sum = 0;
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }

    public static void main(String[] args) {
        int[] samples = {1234, 0, -456, 9, 99999};
        for (int s : samples) {
            System.out.println("Sum of digits of " + s + " = " + sumOfDigits(s));
        }
    }
}

public class Q23_SumOfDigitsTest {

    @Test
    void testMultiDigit() {
        assertEquals(10, DigitSum.sumOfDigits(1234));
    }

    @Test
    void testZero_LoopSkipped() {
        assertEquals(0, DigitSum.sumOfDigits(0));
    }

    @Test
    void testSingleDigit_OneIteration() {
        assertEquals(9, DigitSum.sumOfDigits(9));
    }

    @Test
    void testNegativeNumber() {
        assertEquals(15, DigitSum.sumOfDigits(-456));
    }

    @Test
    void testAllNines() {
        assertEquals(45, DigitSum.sumOfDigits(99999));
    }

    @Test
    void testNumberWithZeros() {
        assertEquals(3, DigitSum.sumOfDigits(1002));
    }

    @Test
    void testMaxInt() {
        // 2147483647 -> 2+1+4+7+4+8+3+6+4+7 = 46
        assertEquals(46, DigitSum.sumOfDigits(Integer.MAX_VALUE));
    }
}
