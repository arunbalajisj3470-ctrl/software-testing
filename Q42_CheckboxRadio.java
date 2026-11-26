// Q42: Selenium program to select checkboxes and a radio button on a form in Chrome.
// Replace the URL and locators with those of the real form.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Q42_CheckboxRadio {

    static final String FORM_URL = "https://YOUR-APP/survey";   // <-- set this

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(FORM_URL);

            // Checkboxes: click only if not already selected
            WebElement java = driver.findElement(By.id("skill-java"));
            if (!java.isSelected()) java.click();

            WebElement sql = driver.findElement(By.id("skill-sql"));
            if (!sql.isSelected()) sql.click();

            // Radio button: select the "Male" option in the gender group
            WebElement male = driver.findElement(By.cssSelector("input[type='radio'][value='male']"));
            if (!male.isSelected()) male.click();

            System.out.println("Java checked: " + java.isSelected());
            System.out.println("SQL checked: " + sql.isSelected());
            System.out.println("Male selected: " + male.isSelected());
        } finally {
            driver.quit();
        }
    }
}
