// Q37: Selenium script to automate a search on Google using Chrome.

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Q37_GoogleSearch {

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://www.google.com");

            WebElement box = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(By.name("q")));
            box.sendKeys("Selenium WebDriver");
            box.sendKeys(Keys.ENTER);

            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.titleContains("Selenium WebDriver"));

            System.out.println("Search completed. Title: " + driver.getTitle());
        } finally {
            driver.quit();
        }
    }
}
