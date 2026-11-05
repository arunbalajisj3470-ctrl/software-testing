import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q26: JUnit test case to find the largest element in an array (white-box testing).
 *
 * White-box analysis of findLargest():
 *   Branch 1: array null or empty -> IllegalArgumentException
 *   Loop    : for i = 1 .. length-1
 *             - 0 iterations (single element array)
 *             - 1+ iterations
 *             - condition (arr[i] > max) TRUE  (new max found)
 *             - condition (arr[i] > max) FALSE (no change)
 *   Tests below cover each branch, each loop scenario, and max at first / middle / last position.
 */
class ArrayUtil {

    public static int findLargest(int[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    public static void main(String[] args) {
        int[] data = {12, 45, 7, 89, 23};
        System.out.println("Largest element = " + findLargest(data));
    }
}

public class Q26_LargestInArrayTest {

    @Test
    void testMaxInMiddle() {
        assertEquals(89, ArrayUtil.findLargest(new int[]{12, 45, 7, 89, 23}));
    }

    @Test
    void testMaxAtFirstPosition_ConditionAlwaysFalse() {
        assertEquals(100, ArrayUtil.findLargest(new int[]{100, 5, 20, 3}));
    }

    @Test
    void testMaxAtLastPosition() {
        assertEquals(50, ArrayUtil.findLargest(new int[]{1, 2, 3, 50}));
    }

    @Test
    void testSingleElement_LoopNotExecuted() {
        assertEquals(42, ArrayUtil.findLargest(new int[]{42}));
    }

    @Test
    void testAllNegativeNumbers() {
        assertEquals(-2, ArrayUtil.findLargest(new int[]{-10, -5, -2, -8}));
    }

    @Test
    void testAllEqualElements() {
        assertEquals(7, ArrayUtil.findLargest(new int[]{7, 7, 7, 7}));
    }

    @Test
    void testDuplicateMaximum() {
        assertEquals(9, ArrayUtil.findLargest(new int[]{9, 3, 9, 1}));
    }

    @Test
    void testIntegerLimits() {
        assertEquals(Integer.MAX_VALUE,
                ArrayUtil.findLargest(new int[]{Integer.MIN_VALUE, 0, Integer.MAX_VALUE}));
    }

    @Test
    void testEmptyArrayThrows() {
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.findLargest(new int[]{}));
    }

    @Test
    void testNullArrayThrows() {
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.findLargest(null));
    }
}
