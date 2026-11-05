import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q28: JUnit program to check whether a number is an Armstrong number.
 *
 * An Armstrong number equals the sum of its digits each raised to the power
 * of the number of digits.  e.g. 153 = 1^3 + 5^3 + 3^3
 *
 * White-box analysis of isArmstrong():
 *   Branch 1: n < 0                -> false
 *   Loop 1  : count digits (while temp != 0)
 *   Loop 2  : sum of digit^count   (while temp != 0)
 *   Branch 2: sum == n  -> true / false
 *   Tests cover each branch, 0-iteration loops (n = 0), and 1..4 digit numbers.
 */
class ArmstrongChecker {

    public static boolean isArmstrong(int n) {
        if (n < 0) {
            return false;
        }
        int digits = String.valueOf(n).length();
        int temp = n;
        long sum = 0;
        while (temp != 0) {
            int d = temp % 10;
            sum += (long) Math.pow(d, digits);
            temp /= 10;
        }
        return sum == n;
    }

    public static void main(String[] args) {
        int[] samples = {153, 370, 123, 9474, 0};
        for (int s : samples) {
            System.out.println(s + (isArmstrong(s) ? " is an Armstrong number" : " is NOT an Armstrong number"));
        }
    }
}

public class Q28_ArmstrongNumberTest {

    @Test
    void testThreeDigitArmstrongNumbers() {
        assertTrue(ArmstrongChecker.isArmstrong(153));
        assertTrue(ArmstrongChecker.isArmstrong(370));
        assertTrue(ArmstrongChecker.isArmstrong(371));
        assertTrue(ArmstrongChecker.isArmstrong(407));
    }

    @Test
    void testFourDigitArmstrongNumber() {
        assertTrue(ArmstrongChecker.isArmstrong(9474));
    }

    @Test
    void testSingleDigitNumbersAreArmstrong() {
        assertTrue(ArmstrongChecker.isArmstrong(0));
        assertTrue(ArmstrongChecker.isArmstrong(1));
        assertTrue(ArmstrongChecker.isArmstrong(9));
    }

    @Test
    void testNonArmstrongNumbers() {
        assertFalse(ArmstrongChecker.isArmstrong(123));
        assertFalse(ArmstrongChecker.isArmstrong(100));
        assertFalse(ArmstrongChecker.isArmstrong(10));
        assertFalse(ArmstrongChecker.isArmstrong(9475));
    }

    @Test
    void testNegativeNumberIsNotArmstrong() {
        assertFalse(ArmstrongChecker.isArmstrong(-153));
    }
}
