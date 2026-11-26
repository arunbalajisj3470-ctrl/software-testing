// Q38: Selenium program to automate user registration form filling (name, email, password, submit) in Chrome.
// Replace REGISTER_URL and the locators (By.id) with those of the real form.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Q38_RegistrationForm {

    static final String REGISTER_URL = "https://YOUR-SITE/register";   // <-- set this

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(REGISTER_URL);

            driver.findElement(By.id("name")).sendKeys("Arun Balaji");
            driver.findElement(By.id("email")).sendKeys("arun.test@example.com");
            driver.findElement(By.id("password")).sendKeys("Secure@123");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            System.out.println("Registration form submitted. Current URL: " + driver.getCurrentUrl());
        } finally {
            driver.quit();
        }
    }
}
