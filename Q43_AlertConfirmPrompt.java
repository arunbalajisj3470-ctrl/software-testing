// Q43: Selenium script to handle JavaScript alert, confirm and prompt pop-ups in Chrome.
// Uses a public demo page that triggers each dialog type from a button.

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Q43_AlertConfirmPrompt {

    static final String DEMO_URL = "https://the-internet.herokuapp.com/javascript_alerts";

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(DEMO_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 1. Simple alert: accept it
            driver.findElement(By.xpath("//button[text()='Click for JS Alert']")).click();
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Alert text: " + alert.getText());
            alert.accept();

            // 2. Confirm: dismiss it (Cancel)
            driver.findElement(By.xpath("//button[text()='Click for JS Confirm']")).click();
            Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Confirm text: " + confirm.getText());
            confirm.dismiss();
            System.out.println("Confirm result: " + driver.findElement(By.id("result")).getText());

            // 3. Prompt: type a value and accept
            driver.findElement(By.xpath("//button[text()='Click for JS Prompt']")).click();
            Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
            prompt.sendKeys("Selenium");
            prompt.accept();
            System.out.println("Prompt result: " + driver.findElement(By.id("result")).getText());
        } finally {
            driver.quit();
        }
    }
}
