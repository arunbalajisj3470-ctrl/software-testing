// Q35: Check whether a given number is a palindrome, and verify output using white-box testing.
// Branches covered: negative numbers, single digit, even/odd digit counts, non-palindromes.

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Scanner;

public class Q35_PalindromeCheck {

    static boolean isPalindrome(int n) {
        if (n < 0) return false;                 // branch: negatives are not palindromes
        if (n < 10) return true;                 // branch: single digit
        int original = n;
        int reversed = 0;
        while (n > 0) {                          // branch: digit-reversal loop
            reversed = reversed * 10 + n % 10;
            n /= 10;
        }
        return original == reversed;
    }

    @Test
    void negativeNumberIsNotPalindrome() {
        assertFalse(isPalindrome(-121));
    }

    @Test
    void singleDigitIsPalindrome() {
        assertTrue(isPalindrome(7));
        assertTrue(isPalindrome(0));
    }

    @Test
    void oddLengthPalindrome() {
        assertTrue(isPalindrome(12321));
    }

    @Test
    void evenLengthPalindrome() {
        assertTrue(isPalindrome(1221));
    }

    @Test
    void nonPalindrome() {
        assertFalse(isPalindrome(123));
    }

    @Test
    void endsWithZeroIsNotPalindrome() {
        assertFalse(isPalindrome(120));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a number: ");
        int n = in.nextInt();
        System.out.println(n + (isPalindrome(n) ? " is a palindrome." : " is not a palindrome."));
        in.close();
    }
}
