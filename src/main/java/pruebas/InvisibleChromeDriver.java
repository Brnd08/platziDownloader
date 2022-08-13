package pruebas;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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

    public static String find_current_video_title(WebDriver driver) {
        String lineContent = null;
        try {
            lineContent = driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView-type" +
                    "--video > div.MaterialView-video > div.MaterialView-content > div > div.Header.material-video > " +
                    "div.Header-class > div > h1")).getText();
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET ELEMENT BY CSS-SELECTOR");
        }
        return lineContent;
    }

    public static int find_number_of_videos(WebDriver driver) {
        int number_of_videos = 0;
        try {
            String [] chapters_info = get_numeric_video_info(driver);
            number_of_videos = Integer.parseInt(chapters_info[1]);
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE TOTAL NUMBER OF VIDEOS IN THIS COURSE");
        }
        return number_of_videos;
    }
    public static String[] get_numeric_video_info(WebDriver driver){
        try{
        String video_info_text =
                driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView-" +
                        "type--video > div.MaterialView-video > div.MaterialView-content > div > div." +
                        "Header.material-video > div.Header-class > div > span")).getText();
        return video_info_text.split("/");
        }catch(Exception e){
            System.out.println("\n FAIL TO GET THE NUMERIC VIDEO INFO\n");
            throw new RuntimeException(e);
        }
    }
    
    public static int find_current_video_number(WebDriver driver) {
        int video_number = 0;
        try {
            String [] chapters_info = get_numeric_video_info(driver);
            video_number = Integer.parseInt(chapters_info[0]);
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE TOTAL NUMBER OF VIDEOS IN THIS COURSE");
        }
        return video_number;
    }

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver;
        driver = new ChromeDriver(create_invisible_chrome_options());

        String url_course = "https://platzi.com/clases/2793-computacion-basica/46747-bienvenida-al-curso";
        driver.get(url_course);

        String soutformat = "%-40s --> %-30s \n";


        String current_video_title = find_current_video_title(driver);
        System.out.format(soutformat,"The current video title is", current_video_title);

        int current_video_number = find_current_video_number(driver);
        System.out.format(soutformat,"The current video number is", current_video_number);

        int total_videos_number = find_number_of_videos(driver);
        System.out.format(soutformat, "The total number of videos", total_videos_number);

//        driver.quit();
    }
}