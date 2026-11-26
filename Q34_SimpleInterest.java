// Q34: Program using functions to calculate Simple Interest.
// Senior citizens get 12% p.a.; all other customers get 10% p.a.
// Output is verified with white-box tests (JUnit 5) covering both rate branches.
//
// Formula: SI = (P * R * T) / 100

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Scanner;

public class Q34_SimpleInterest {

    // Function: returns the applicable annual rate (branch on customer type)
    static double interestRate(boolean seniorCitizen) {
        if (seniorCitizen) {
            return 12.0;
        }
        return 10.0;
    }

    // Function: computes simple interest
    static double simpleInterest(double principal, double rate, double years) {
        return (principal * rate * years) / 100.0;
    }

    // ---- White-box verification ----
    @Test
    void seniorCitizenGetsTwelvePercent() {
        assertEquals(12.0, interestRate(true));
    }

    @Test
    void otherCustomersGetTenPercent() {
        assertEquals(10.0, interestRate(false));
    }

    @Test
    void simpleInterestForSeniorCitizen() {
        double rate = interestRate(true);
        // 10000 * 12 * 2 / 100 = 2400
        assertEquals(2400.0, simpleInterest(10000, rate, 2), 0.0001);
    }

    @Test
    void simpleInterestForRegularCustomer() {
        double rate = interestRate(false);
        // 5000 * 10 * 3 / 100 = 1500
        assertEquals(1500.0, simpleInterest(5000, rate, 3), 0.0001);
    }

    @Test
    void zeroYearsGivesZeroInterest() {
        assertEquals(0.0, simpleInterest(5000, 10, 0), 0.0001);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter principal amount: ");
        double p = in.nextDouble();
        System.out.print("Enter time in years: ");
        double t = in.nextDouble();
        System.out.print("Is the customer a senior citizen? (true/false): ");
        boolean senior = in.nextBoolean();

        double rate = interestRate(senior);
        double si = simpleInterest(p, rate, t);
        System.out.printf("Rate applied: %.1f%%%n", rate);
        System.out.printf("Simple Interest: %.2f%n", si);
        System.out.printf("Total amount: %.2f%n", p + si);
        in.close();
    }
}
