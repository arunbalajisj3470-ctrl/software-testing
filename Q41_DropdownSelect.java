// Q41: Selenium script to select a dropdown option using selectByVisibleText in Chrome.
// Replace the URL and the dropdown locator with those of the real page.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class Q41_DropdownSelect {

    static final String PAGE_URL = "https://YOUR-APP/form";   // <-- set this

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(PAGE_URL);

            Select country = new Select(driver.findElement(By.id("country")));
            country.selectByVisibleText("India");

            String selected = country.getFirstSelectedOption().getText();
            System.out.println("Selected: " + selected);
            System.out.println(selected.equals("India") ? "PASS" : "FAIL");
        } finally {
            driver.quit();
        }
    }
}
