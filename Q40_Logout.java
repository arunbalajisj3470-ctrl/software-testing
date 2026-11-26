// Q40: Selenium script to log in and then log out of a web application using Chrome.
// Replace the URLs and locators with the real application's values.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Q40_Logout {

    static final String LOGIN_URL = "https://YOUR-APP/login";   // <-- set this
    static final String USER = "testuser";
    static final String PASS = "testpass";

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(LOGIN_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            driver.findElement(By.id("username")).sendKeys(USER);
            driver.findElement(By.id("password")).sendKeys(PASS);
            driver.findElement(By.id("loginBtn")).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logoutBtn"))).click();

            // Verify logout: the login form should be visible again
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            System.out.println("PASS: logout successful, login page displayed.");
        } finally {
            driver.quit();
        }
    }
}
