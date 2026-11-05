import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q27: Average of n numbers using functions; verified with white-box testing.
 *
 * Functions:
 *   calculateSum(double[])     - adds all elements
 *   calculateAverage(double[]) - sum / n
 *
 * White-box analysis:
 *   calculateAverage(): Branch 1 -> null/empty array throws exception; Branch 2 -> valid array
 *   calculateSum()    : loop runs 0 / 1 / many times
 *   Tests cover both branches and all loop scenarios.
 */
class AverageCalculator {

    public static double calculateSum(double[] numbers) {
        double sum = 0;
        for (double n : numbers) {
            sum += n;
        }
        return sum;
    }

    public static double calculateAverage(double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("At least one number is required");
        }
        return calculateSum(numbers) / numbers.length;
    }

    public static void main(String[] args) {
        double[] data = {10, 20, 30, 40, 50};
        System.out.println("Sum     = " + calculateSum(data));
        System.out.println("Average = " + calculateAverage(data));
    }
}

public class Q27_AverageOfNumbersTest {

    private static final double DELTA = 0.0001;

    @Test
    void testAverageOfThreeNumbers() {
        assertEquals(20.0, AverageCalculator.calculateAverage(new double[]{10, 20, 30}), DELTA);
    }

    @Test
    void testSingleNumber() {
        assertEquals(5.0, AverageCalculator.calculateAverage(new double[]{5}), DELTA);
    }

    @Test
    void testFractionalResult() {
        assertEquals(1.5, AverageCalculator.calculateAverage(new double[]{1, 2}), DELTA);
    }

    @Test
    void testNegativeNumbersCancelOut() {
        assertEquals(0.0, AverageCalculator.calculateAverage(new double[]{-10, 10}), DELTA);
    }

    @Test
    void testAllNegativeNumbers() {
        assertEquals(-4.0, AverageCalculator.calculateAverage(new double[]{-2, -4, -6}), DELTA);
    }

    @Test
    void testDecimalNumbers() {
        assertEquals(2.5, AverageCalculator.calculateAverage(new double[]{1.5, 2.5, 3.5}), DELTA);
    }

    @Test
    void testSumFunction() {
        assertEquals(150.0, AverageCalculator.calculateSum(new double[]{10, 20, 30, 40, 50}), DELTA);
        assertEquals(0.0, AverageCalculator.calculateSum(new double[]{}), DELTA);  // loop not executed
    }

    @Test
    void testEmptyArrayThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> AverageCalculator.calculateAverage(new double[]{}));
    }

    @Test
    void testNullArrayThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> AverageCalculator.calculateAverage(null));
    }
}
