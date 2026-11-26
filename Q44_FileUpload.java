// Q44: Selenium program to upload a file on a web application using Chrome.
// File inputs accept a local absolute path via sendKeys, so no OS dialog is needed.
// Replace the URL, locator and file path.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Q44_FileUpload {

    static final String UPLOAD_URL = "https://the-internet.herokuapp.com/upload";
    static final String FILE_PATH = "/home/claude/sample.txt";   // <-- a file that exists on your machine

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(UPLOAD_URL);

            driver.findElement(By.id("file-upload")).sendKeys(FILE_PATH);
            driver.findElement(By.id("file-submit")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploaded-files")));

            System.out.println("Uploaded file shown: " + driver.findElement(By.id("uploaded-files")).getText());
        } finally {
            driver.quit();
        }
    }
}
