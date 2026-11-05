import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q25: Calculate student grade from marks using functions; verified with white-box testing.
 *
 * Grading scale:
 *   90-100 -> A | 80-89 -> B | 70-79 -> C | 60-69 -> D | 50-59 -> E | 0-49 -> F
 *
 * White-box analysis of calculateGrade():
 *   Branch 0: marks < 0 or > 100 -> IllegalArgumentException
 *   Branches 1-6: one per grade band (if / else-if chain)
 *   Tests cover every branch and the boundary value on each side of every condition
 *   (condition / boundary-value coverage).
 */
class GradeCalculator {

    public static char calculateGrade(int marks) {
        if (marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Marks must be between 0 and 100");
        }
        if (marks >= 90) {
            return 'A';
        } else if (marks >= 80) {
            return 'B';
        } else if (marks >= 70) {
            return 'C';
        } else if (marks >= 60) {
            return 'D';
        } else if (marks >= 50) {
            return 'E';
        } else {
            return 'F';
        }
    }

    public static void main(String[] args) {
        int[] samples = {95, 85, 72, 65, 55, 30};
        for (int m : samples) {
            System.out.println("Marks " + m + " -> Grade " + calculateGrade(m));
        }
    }
}

public class Q25_StudentGradeTest {

    @Test
    void testGradeA() {
        assertEquals('A', GradeCalculator.calculateGrade(95));
        assertEquals('A', GradeCalculator.calculateGrade(90));   // lower boundary
        assertEquals('A', GradeCalculator.calculateGrade(100));  // upper boundary
    }

    @Test
    void testGradeB() {
        assertEquals('B', GradeCalculator.calculateGrade(89));   // just below A
        assertEquals('B', GradeCalculator.calculateGrade(80));
    }

    @Test
    void testGradeC() {
        assertEquals('C', GradeCalculator.calculateGrade(79));
        assertEquals('C', GradeCalculator.calculateGrade(70));
    }

    @Test
    void testGradeD() {
        assertEquals('D', GradeCalculator.calculateGrade(69));
        assertEquals('D', GradeCalculator.calculateGrade(60));
    }

    @Test
    void testGradeE() {
        assertEquals('E', GradeCalculator.calculateGrade(59));
        assertEquals('E', GradeCalculator.calculateGrade(50));
    }

    @Test
    void testGradeF() {
        assertEquals('F', GradeCalculator.calculateGrade(49));
        assertEquals('F', GradeCalculator.calculateGrade(0));
    }

    @Test
    void testNegativeMarksInvalid() {
        assertThrows(IllegalArgumentException.class, () -> GradeCalculator.calculateGrade(-1));
    }

    @Test
    void testMarksAbove100Invalid() {
        assertThrows(IllegalArgumentException.class, () -> GradeCalculator.calculateGrade(101));
    }
}
