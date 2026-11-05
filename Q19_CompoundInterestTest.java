import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q19: Compound interest using functions, verified with white-box testing + JUnit assertions.
 *
 * Formulas:
 *   Amount = P * (1 + r/(100*n)) ^ (n*t)
 *   Compound Interest = Amount - P
 *
 * White-box analysis of calculateAmount():
 *   Branch 1: principal < 0 / rate < 0 / time < 0 / n <= 0  -> throws exception
 *   Branch 2: valid input -> computes formula
 *   Tests cover both branches, each invalid condition, and boundary values (0 rate, 0 time).
 */
class CompoundInterestCalculator {

    public static double calculateAmount(double principal, double rate, double time, int timesPerYear) {
        if (principal < 0 || rate < 0 || time < 0 || timesPerYear <= 0) {
            throw new IllegalArgumentException("Invalid input values");
        }
        return principal * Math.pow(1 + rate / (100.0 * timesPerYear), timesPerYear * time);
    }

    public static double calculateCompoundInterest(double principal, double rate, double time, int timesPerYear) {
        return calculateAmount(principal, rate, time, timesPerYear) - principal;
    }

    public static void main(String[] args) {
        double p = 10000, r = 10, t = 2;
        int n = 1;
        System.out.printf("Amount           : %.2f%n", calculateAmount(p, r, t, n));
        System.out.printf("Compound Interest: %.2f%n", calculateCompoundInterest(p, r, t, n));
    }
}

public class Q19_CompoundInterestTest {

    private static final double DELTA = 0.01;

    @Test
    void testAnnualCompounding() {
        // 10000 @ 10% for 2 years, yearly -> 12100, CI = 2100
        assertEquals(12100.00, CompoundInterestCalculator.calculateAmount(10000, 10, 2, 1), DELTA);
        assertEquals(2100.00, CompoundInterestCalculator.calculateCompoundInterest(10000, 10, 2, 1), DELTA);
    }

    @Test
    void testQuarterlyCompounding() {
        // 5000 @ 8% for 3 years, quarterly -> 6341.21, CI = 1341.21
        assertEquals(6341.21, CompoundInterestCalculator.calculateAmount(5000, 8, 3, 4), DELTA);
        assertEquals(1341.21, CompoundInterestCalculator.calculateCompoundInterest(5000, 8, 3, 4), DELTA);
    }

    @Test
    void testZeroRate_NoInterest() {
        assertEquals(0.0, CompoundInterestCalculator.calculateCompoundInterest(1000, 0, 5, 1), DELTA);
    }

    @Test
    void testZeroTime_NoInterest() {
        assertEquals(0.0, CompoundInterestCalculator.calculateCompoundInterest(1000, 5, 0, 1), DELTA);
    }

    @Test
    void testZeroPrincipal() {
        assertEquals(0.0, CompoundInterestCalculator.calculateAmount(0, 10, 2, 1), DELTA);
    }

    @Test
    void testInvalidPrincipal() {
        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(-1, 10, 2, 1));
    }

    @Test
    void testInvalidRate() {
        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(1000, -10, 2, 1));
    }

    @Test
    void testInvalidTime() {
        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(1000, 10, -2, 1));
    }

    @Test
    void testInvalidCompoundingFrequency() {
        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(1000, 10, 2, 0));
    }
}
