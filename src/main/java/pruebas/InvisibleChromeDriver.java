package pruebas;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static pruebas.DownloaderHelper.download_bytes;

public class InvisibleChromeDriver {

    public static String get_current_stream_url(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        ArrayList<Map<String, String>> entries = (ArrayList<Map<String, String>>) js.executeScript("return window.performance.getEntries();");
        for (Map<String, String> entry1 : entries) {
            String entry = entry1.get("name");
            if (entry.contains(".ism") && entry.contains("/manifest(") || entry.contains(".m3u8"))
                return entry;
        }
        throw new RuntimeException("COULDN'T GET THE STREAM URL");
    }

    public static void download_stream(String input_url, String output_file_path, String video_name) {
        try {
            FFmpeg ffmpeg = new FFmpeg("src/main/java/pruebas/ffmpeg.exe");
            FFprobe ffprobe = new FFprobe("src/main/java/pruebas/ffprobe.exe");

            FFmpegBuilder builder = new FFmpegBuilder()

                    .setInput(input_url)     // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists

                    .addOutput(output_file_path)   // Filename for the destination

                    .setAudioChannels(1)         // Mono audio
                    .setAudioCodec("copy")        // using the aac codec

                    .setVideoCodec("copy")     // Video using x264
                    .setAudioBitStreamFilter("aac_adtstoasc")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

// Run a one-pass encode
            executor.createJob(builder).run();

        } catch (Exception e) {
            System.out.println("\nCOULDN'T DOWNLOAD THE VIDEO FOR THIS COURSE\n");
            throw new RuntimeException(e);
        }
    }

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
        File livecatExtension = new File("src/main/java/pruebas/livecat.crx");
        options.addExtensions(livecatExtension);
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
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
            String[] chapters_info = get_numeric_video_info(driver);
            number_of_videos = Integer.parseInt(chapters_info[1]);
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE TOTAL NUMBER OF VIDEOS IN THIS COURSE");
        }
        return number_of_videos;
    }

    public static String[] get_numeric_video_info(WebDriver driver) {
        try {
            String video_info_text =
                    driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView-" +
                            "type--video > div.MaterialView-video > div.MaterialView-content > div > div." +
                            "Header.material-video > div.Header-class > div > span")).getText();
            return video_info_text.split("/");
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE NUMERIC VIDEO INFO\n");
            throw new RuntimeException(e);
        }
    }

    public static String get_current_course(WebDriver driver) {
        try {
            String video_info_text =
                    driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView-type" +
                            "--video > div.MaterialView-video > div.MaterialView-content > div > div.Header" +
                            ".material-video > div.Header-course > div.Header-course-info > div > a > h2")).getText();
            return video_info_text;
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE CURRENT COURSE\n");
            throw new RuntimeException(e);
        }
    }

    public static int find_current_video_number(WebDriver driver) {
        int video_number = 0;
        try {
            String[] chapters_info = get_numeric_video_info(driver);
            video_number = Integer.parseInt(chapters_info[0]);
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE TOTAL NUMBER OF VIDEOS IN THIS COURSE");
        }
        return video_number;
    }

    public static void download_current_video_files(WebDriver driver, String file_name) {
        WebElement download_all_files_button;
        String link;
        try {
            download_all_files_button = driver.findElement(By.className("FilesTree-download"));
            link = download_all_files_button.getAttribute("href");
        } catch (Exception e) {
            System.out.println("COULDN'T FIND FILES FOR THIS VIDEO \t");
            return;
        }
        try {
            URL download_url = new URL(link);
            String extension = new StringBuilder(link).substring(link.lastIndexOf("."));
            download_bytes("C:\\Users\\brdn\\OneDrive - Instituto Politecnico Nacional\\Desktop", file_name + extension, download_url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver;
        driver = new ChromeDriver(create_invisible_chrome_options());

        String url_course = "https://platzi.com/clases/3208-programacion-basica/51979-crea-tu-primer-sitio-web";
        driver.get(url_course);

        String printFormat = "%-40s --> %-30s \n";


        String current_video_title = find_current_video_title(driver);
        System.out.format(printFormat, "The current video TITLE is", current_video_title);

        String current_video_course = get_current_course(driver);
        System.out.format(printFormat, "The current video COURSE is", current_video_course);

        int current_video_number = find_current_video_number(driver);
        System.out.format(printFormat, "The current video NUMBER is", current_video_number);

        int total_videos_number = find_number_of_videos(driver);
        System.out.format(printFormat, "The total NUMBER OF VIDEOS is", total_videos_number);

        System.out.println("STARTING DOWNLOADING VIDEO FILES...");

        String file_to_download_name = current_video_title + " - " + driver.findElement(By.className("FilesAndLinks-title")).getText();
        try {
            download_current_video_files(driver, file_to_download_name);
        } catch (Exception e) {
            System.out.println("COULDN'T FIND FILES FOR THIS VIDEO \t" + e.getMessage());
        }

        String streaming_url = get_current_stream_url(driver);
        System.out.format(printFormat, "Streaming link", streaming_url);

        System.out.format(printFormat, "STARTING DOWNLOADING CURRENT VIDEO", current_video_title);

        String video_name = current_video_number + ".- " + current_video_title + ".mp4";
        download_stream(streaming_url, "C:\\Users\\brdn\\OneDrive - Instituto Politecnico Nacional\\Desktop\\" + video_name, current_video_title);
        System.out.println("THE VIDEO FOR THIS COURSE WAS SUCCESFULLY DOWNLOADED");

//        driver.quit();
    }
}