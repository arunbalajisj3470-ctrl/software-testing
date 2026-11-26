// Q31: Demonstrate the working of JUnit to reverse a word and validate with assert statements.
// Requires JUnit 5 (junit-jupiter) on the classpath.

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Q31_ReverseWordTest {

    // Unit under test
    static String reverseWord(String word) {
        if (word == null) return null;
        return new StringBuilder(word).reverse().toString();
    }

    @Test
    void reversesSimpleWord() {
        assertEquals("olleh", reverseWord("hello"));
    }

    @Test
    void reversesSingleCharacter() {
        assertEquals("a", reverseWord("a"));
    }

    @Test
    void reversesEmptyString() {
        assertEquals("", reverseWord(""));
    }

    @Test
    void reversingTwiceReturnsOriginal() {
        String word = "JUnit";
        assertEquals(word, reverseWord(reverseWord(word)));
    }

    @Test
    void returnsNullForNullInput() {
        assertNull(reverseWord(null));
    }

    // Simple runner so the program can also be executed without an IDE
    public static void main(String[] args) {
        System.out.println("reverse(\"hello\") = " + reverseWord("hello"));
        System.out.println("Run this class with JUnit to see the assertions pass.");
    }
}
