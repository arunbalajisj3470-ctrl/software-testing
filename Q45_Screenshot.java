// Q45: Selenium script to capture a screenshot of a web page and save it to the local system using Chrome.

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Q45_Screenshot {

    static final String PAGE_URL = "https://www.google.com";
    static final String SAVE_PATH = "screenshot.png";   // saved in the working directory

    public static void main(String[] args) throws Exception {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(PAGE_URL);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dest = Paths.get(SAVE_PATH).toAbsolutePath();
            Files.copy(src.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved to: " + dest);
        } finally {
            driver.quit();
        }
    }
}
