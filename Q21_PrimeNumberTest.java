import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q21: Check whether a number is prime; verified with white-box testing + JUnit assertions.
 *
 * White-box analysis of isPrime():
 *   Branch A: n <= 1            -> false
 *   Branch B: n == 2 (or <= 3)  -> true
 *   Branch C: n % 2 == 0        -> false
 *   Loop    : i from 3 to sqrt(n) step 2
 *             - loop not entered (n = 9 is entered, n = 5/7 not entered beyond check)
 *             - divisor found     -> false
 *             - no divisor found  -> true
 *   Tests below cover all branches and loop outcomes.
 */
class PrimeChecker {

    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0) {
            return false;
        }
        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int[] samples = {1, 2, 17, 25, 97};
        for (int s : samples) {
            System.out.println(s + (isPrime(s) ? " is Prime" : " is Not Prime"));
        }
    }
}

public class Q21_PrimeNumberTest {

    @Test
    void testNumbersLessThanTwoAreNotPrime() {
        assertFalse(PrimeChecker.isPrime(1));
        assertFalse(PrimeChecker.isPrime(0));
        assertFalse(PrimeChecker.isPrime(-7));
    }

    @Test
    void testSmallPrimes() {
        assertTrue(PrimeChecker.isPrime(2));
        assertTrue(PrimeChecker.isPrime(3));
    }

    @Test
    void testEvenCompositeNumbers() {
        assertFalse(PrimeChecker.isPrime(4));
        assertFalse(PrimeChecker.isPrime(100));
    }

    @Test
    void testOddCompositeNumbers_LoopFindsDivisor() {
        assertFalse(PrimeChecker.isPrime(9));
        assertFalse(PrimeChecker.isPrime(25));
        assertFalse(PrimeChecker.isPrime(91));   // 7 * 13
    }

    @Test
    void testOddPrimes_LoopCompletesWithoutDivisor() {
        assertTrue(PrimeChecker.isPrime(5));
        assertTrue(PrimeChecker.isPrime(17));
        assertTrue(PrimeChecker.isPrime(97));
        assertTrue(PrimeChecker.isPrime(7919));
    }

    @Test
    void testPerfectSquareOfPrime() {
        assertFalse(PrimeChecker.isPrime(49));
    }

    @Test
    void testLargePrimeNoOverflow() {
        assertTrue(PrimeChecker.isPrime(2147483647)); // Mersenne prime 2^31 - 1
    }
}
