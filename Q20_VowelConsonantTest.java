import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Q20: JUnit program to count vowels and consonants in a string, validated with assertions.
 *
 * White-box analysis of the counting loop:
 *   - null / empty guard branch
 *   - loop executes 0 times, 1 time, many times
 *   - inside loop: letter? -> vowel? (3 outcomes: vowel, consonant, non-letter)
 *   Tests below cover every branch and loop condition.
 */
class StringAnalyzer {

    private static boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) != -1;
    }

    public static int countVowels(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c) && isVowel(c)) {
                count++;
            }
        }
        return count;
    }

    public static int countConsonants(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c) && !isVowel(c)) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        String text = "Hello World";
        System.out.println("String     : " + text);
        System.out.println("Vowels     : " + countVowels(text));
        System.out.println("Consonants : " + countConsonants(text));
    }
}

public class Q20_VowelConsonantTest {

    @Test
    void testMixedString() {
        assertEquals(3, StringAnalyzer.countVowels("Hello World"));
        assertEquals(7, StringAnalyzer.countConsonants("Hello World"));
    }

    @Test
    void testOnlyVowels() {
        assertEquals(5, StringAnalyzer.countVowels("AEIOU"));
        assertEquals(0, StringAnalyzer.countConsonants("AEIOU"));
    }

    @Test
    void testOnlyConsonants() {
        assertEquals(0, StringAnalyzer.countVowels("xyz"));
        assertEquals(3, StringAnalyzer.countConsonants("xyz"));
    }

    @Test
    void testEmptyString() {
        assertEquals(0, StringAnalyzer.countVowels(""));
        assertEquals(0, StringAnalyzer.countConsonants(""));
    }

    @Test
    void testNullString() {
        assertEquals(0, StringAnalyzer.countVowels(null));
        assertEquals(0, StringAnalyzer.countConsonants(null));
    }

    @Test
    void testDigitsAndSymbolsIgnored() {
        assertEquals(0, StringAnalyzer.countVowels("123 !@#"));
        assertEquals(0, StringAnalyzer.countConsonants("123 !@#"));
    }

    @Test
    void testCaseInsensitivity() {
        assertEquals(2, StringAnalyzer.countVowels("aA"));
        assertEquals(2, StringAnalyzer.countConsonants("bB"));
    }

    @Test
    void testTotalLettersEqualsVowelsPlusConsonants() {
        String s = "Software Testing 101";
        int letters = s.replaceAll("[^A-Za-z]", "").length();
        assertEquals(letters, StringAnalyzer.countVowels(s) + StringAnalyzer.countConsonants(s));
    }
}
