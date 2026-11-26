// Q36: Selenium script to open Chrome and verify the title of the ARMS portal.
// Requires: Selenium 4+ (selenium-java), ChromeDriver managed by Selenium Manager.
// Replace ARMS_URL with the real portal address.

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Q36_ARMS_TitleCheck {

    static final String ARMS_URL = "https://YOUR-ARMS-PORTAL-URL";   // <-- set this
    static final String EXPECTED_TITLE = "ARMS";                     // <-- set to the real title text

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(ARMS_URL);
            String actual = driver.getTitle();
            System.out.println("Page title: " + actual);

            if (actual != null && actual.contains(EXPECTED_TITLE)) {
                System.out.println("PASS: ARMS portal title verified.");
            } else {
                System.out.println("FAIL: expected title containing '" + EXPECTED_TITLE + "'.");
            }
        } finally {
            driver.quit();
        }
    }
}
