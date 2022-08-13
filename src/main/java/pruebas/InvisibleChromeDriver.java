package pruebas;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.Collections;

public class InvisibleChromeDriver {

    public static ChromeOptions create_invisible_chrome_options() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", null);
        options.addArguments("window-size=1920,1080");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        options.addArguments("disable-infobars");
        return options;
    }

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver;
        driver = new ChromeDriver(create_invisible_chrome_options());

        String url_course = "https://platzi.com/clases/2793-computacion-basica/46747-bienvenida-al-curso";
        driver.get(url_course);

        String title = driver.getTitle();
        System.out.println("The page title is: " + title);
        try {
            WebElement linea = driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView-type--video > div.MaterialView-video > div.MaterialView-content > div > div.Header.material-video > div.Header-class > div > h1"));
            String lineContent = linea.getText();
            System.out.println("lineaContent: " + lineContent);
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET ELEMENT BY CSS-SELECTOR");
        }

        driver.quit();
    }
}