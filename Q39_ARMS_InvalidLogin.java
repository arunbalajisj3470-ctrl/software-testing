// Q39: Selenium script to log in to the ARMS portal with invalid credentials and validate the error message.
// Replace the URL and locators with those of the real ARMS login page.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Q39_ARMS_InvalidLogin {

    static final String LOGIN_URL = "https://YOUR-ARMS-PORTAL-URL/login";  // <-- set this
    static final String EXPECTED_ERROR = "Invalid";                        // part of the expected message

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(LOGIN_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")))
                    .sendKeys("wrong_user");
            driver.findElement(By.id("password")).sendKeys("wrong_password");
            driver.findElement(By.id("loginBtn")).click();

            WebElement error = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
            String text = error.getText();
            System.out.println("Error message shown: " + text);

            if (text.contains(EXPECTED_ERROR)) {
                System.out.println("PASS: invalid credentials rejected with an error message.");
            } else {
                System.out.println("FAIL: unexpected error message.");
            }
        } finally {
            driver.quit();
        }
    }
}
