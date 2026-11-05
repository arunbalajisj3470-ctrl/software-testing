import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q29: Convert temperature from Celsius to Fahrenheit; verified with white-box testing.
 *
 * Formula: F = (C * 9 / 5) + 32
 *
 * White-box analysis of celsiusToFahrenheit():
 *   Branch 1: celsius < -273.15 (below absolute zero) -> IllegalArgumentException
 *   Branch 2: valid input -> apply formula
 *   Tests cover both branches plus boundary values (absolute zero, freezing, boiling, -40 crossover).
 */
class TemperatureConverter {

    public static double celsiusToFahrenheit(double celsius) {
        if (celsius < -273.15) {
            throw new IllegalArgumentException("Temperature below absolute zero");
        }
        return (celsius * 9.0 / 5.0) + 32;
    }

    public static void main(String[] args) {
        double[] samples = {0, 100, -40, 37, 25};
        for (double c : samples) {
            System.out.printf("%.1f C = %.1f F%n", c, celsiusToFahrenheit(c));
        }
    }
}

public class Q29_CelsiusToFahrenheitTest {

    private static final double DELTA = 0.0001;

    @Test
    void testFreezingPoint() {
        assertEquals(32.0, TemperatureConverter.celsiusToFahrenheit(0), DELTA);
    }

    @Test
    void testBoilingPoint() {
        assertEquals(212.0, TemperatureConverter.celsiusToFahrenheit(100), DELTA);
    }

    @Test
    void testScalesCrossAtMinus40() {
        assertEquals(-40.0, TemperatureConverter.celsiusToFahrenheit(-40), DELTA);
    }

    @Test
    void testBodyTemperature() {
        assertEquals(98.6, TemperatureConverter.celsiusToFahrenheit(37), DELTA);
    }

    @Test
    void testRoomTemperature() {
        assertEquals(77.0, TemperatureConverter.celsiusToFahrenheit(25), DELTA);
    }

    @Test
    void testAbsoluteZero_Boundary() {
        assertEquals(-459.67, TemperatureConverter.celsiusToFahrenheit(-273.15), DELTA);
    }

    @Test
    void testBelowAbsoluteZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> TemperatureConverter.celsiusToFahrenheit(-300));
    }

    @Test
    void testDecimalInput() {
        assertEquals(68.9, TemperatureConverter.celsiusToFahrenheit(20.5), DELTA);
    }
}
