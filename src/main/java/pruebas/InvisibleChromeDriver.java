package pruebas;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static pruebas.DownloaderHelper.download_bytes;

public class InvisibleChromeDriver {

    static {
        WebDriverManager.chromedriver().setup();
    }

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

    public static void download_stream(String input_url, String output_file_path) {
        try {
            FFmpeg ffmpeg = new FFmpeg("src/main/java/pruebas/ffmpeg.exe");
            FFprobe fFprobe= new FFprobe("src/main/java/pruebas/ffprobe.exe");

            FFmpegBuilder builder = new FFmpegBuilder()

                    .setInput(input_url)     // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists


                    .addOutput("\"" +output_file_path.replaceAll(":","") + ".mp4\"" )   // Filename for the destination

                    .setFormat("mp4")


                    .setAudioChannels(1)         // Mono audio
                    .setAudioCodec("copy")        // using the aac codec

                    .setVideoCodec("copy")     // Video using x264
                    .setAudioBitStreamFilter("aac_adtstoasc")


                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, fFprobe);

// Run a one-pass encode
            executor.createJob(builder).run();

        } catch (Exception e) {
            System.out.println("\nCOULDN'T DOWNLOAD THE VIDEO FOR THIS COURSE " );
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

    public static void download_current_video_files(WebDriver driver, String file_name, String parent_directory) {
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
            download_bytes(parent_directory, file_name + extension, download_url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean page_has_loaded(WebDriver driver, String old_text_value) {

        String current_text_value = find_current_video_title(driver);

        return !old_text_value.equals(current_text_value) && !old_text_value.isBlank() && !old_text_value.isEmpty();
    }

    public static void go_to_next_video_and_wait(WebDriver driver) {
        try {
            WebElement next_video_button = driver.findElement(By.className("Header-course-actions-next"));
            next_video_button.click();
            String old_text_value = find_current_video_title(driver);
            while (!page_has_loaded(driver, old_text_value))
                Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println("\n FAIL TO WAIT UNTIL LOAD PAGE\n");
            throw new RuntimeException(e);
        }
    }

    public static void write_credentials(WebDriver driver, String email, String pass){
        try {
            WebElement email_input = driver.findElement(By.name("email"));
            WebElement pass_input = driver.findElement(By.name("password"));

            WebElement submit_button = driver.findElement(By.cssSelector(".btn-Green.btn--md"));

            email_input.sendKeys(email);
            Thread.sleep(3000);

            pass_input.sendKeys(pass);
            Thread.sleep(3000);


            submit_button.click();
            Thread.sleep(18000);
        }catch(Exception e){
            System.out.println("COULDN'T LOG IN AUTOMATICALLY");
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) {

        WebDriver driver = new ChromeDriver(create_invisible_chrome_options());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        final String printFormat = "%-50s --> %-30s \n";
        final String numeration_separator = ".- ";

        final String first_video_url = "https://platzi.com/clases/3208-programacion-basica/51981-estructura-arbol-html";
        final String platzi_login_url = "https://platzi.com/login";
        JOptionPane.showMessageDialog(null , "Solo necesitas iniciar sesión si no, el programa no tendra" +
                " acceso a los videos, TIENES 10 SEGUNDOS PARA RESOLVER EL CAPCHA SI ES QUE TE SALTA", "Solo recuerda", JOptionPane.INFORMATION_MESSAGE);

        driver.get(platzi_login_url);

        final String mail = "binig23431@kaseig.com";
        final String password = "contra12";

        System.out.format(printFormat, "Credentials", "email: " + mail + " password: " + password);

        write_credentials(driver, mail, password);


        driver.get(first_video_url);

        final String course_name = get_current_course(driver);
        System.out.format(printFormat, "The current video COURSE is", course_name);

        final File parent_directory = new File ("C:\\Users\\brdn\\OneDrive - Instituto Politecnico Nacional\\Desktop\\Platzi-Downloader");

        final File course_directory = new File (parent_directory + File.separator + course_name);
        final File lesson_resources_directory = new File (course_directory + File.separator + "archivos de las clases");



        final int total_videos_number = find_number_of_videos(driver);
        System.out.format(printFormat, "The total NUMBER OF VIDEOS in this course: ", total_videos_number);

        String current_video_title = "Default title";
        int current_video_number = 1;
        String current_files_download_name = "Default files download name";
        String current_stream_url = "Default stream url";
        String current_video_download_name = "Default video download name";

        do {
            current_video_number = find_current_video_number(driver);
            System.out.format(printFormat, "The current video NUMBER is", current_video_number);

            current_video_title = find_current_video_title(driver);
            System.out.format(printFormat, "The current video TITLE is", current_video_title);

            System.out.println("SEARCHING FOR FILES IN THIS VIDEO -------------------------------------------------------------");
            try {
                current_files_download_name = current_video_number + numeration_separator + current_video_title
                        + " - " + driver.findElement(By.className("FilesAndLinks-title")).getText();
                download_current_video_files(driver, current_files_download_name, lesson_resources_directory.toString());
            } catch (Exception e) {
                System.out.println("ATTENTION: COULDN'T FIND FILES FOR THIS VIDEO \t");
            }

            current_stream_url = get_current_stream_url(driver);
            System.out.format(printFormat, "Streaming link", current_stream_url);

            System.out.format(printFormat, "STARTING DOWNLOADING CURRENT VIDEO", current_video_title);
            current_video_download_name = current_video_number + numeration_separator + current_video_title;

            if(!course_directory.exists())
                course_directory.mkdirs();

            download_stream(current_stream_url, course_directory + File.separator + current_video_download_name);
            System.out.println(current_video_download_name + " WAS SUCCESSFULLY DOWNLOADED \n");

            go_to_next_video_and_wait(driver);

        } while (current_video_number < total_videos_number);

        System.out.println("ALL THE VIDEOS FOR THIS COURSE WAS SUCCESSFULLY DOWNLOADED");
        System.out.println(current_video_number + "/" + total_videos_number +" WERE DOWNLOADED FROM THE COURSE ->> " + course_name);
        System.out.println("ALL THE FILES ARE IN THE FOLLOWING PATH" + course_directory);

//        driver.quit();
    }
}