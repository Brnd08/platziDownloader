package pruebas;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static pruebas.DownloaderHelper.*;


public class InvisibleChromeDriver {

    public static final String printFormat = " %-60s || %-30s \n";
    public static final String imagesFormat = "png";
    public static final String html_format = "<!DOCTYPE html>" + "<head><title>$title</title></head>" + "<body>$body</body>" + "</html>";


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

    public static void download_stream(String input_url, String output_file_path, String ffmpeg_path) {
        String output = "\"" + output_file_path + "\"";
        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpeg_path);
            FFmpegBuilder builder = new FFmpegBuilder()

                    .setInput(input_url)     // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists

                    .addOutput(output_file_path)   // Filename for the destination

                    .setAudioChannels(1)         // Mono audio
                    .setAudioCodec("copy")        // using the aac codec

                    .setVideoCodec("copy")     // Video using x264
                    .setAudioBitStreamFilter("aac_adtstoasc").done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

// Run a one-pass encode
            executor.createJob(builder).run();

        } catch (Exception e) {
            System.out.println("\nCOULDN'T DOWNLOAD THE VIDEO FOR THIS COURSE " + output + "\n");
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
        options.addArguments("--start-maximized");
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

    public static String find_current_lecture_title(WebDriver driver) {
        String lineContent = null;
        try {
            lineContent = driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView" +
                    "-type--lecture > div.MaterialView-video > div > div > div.Header.material-lecture > " +
                    "div.Header-class > div.Header-class-title > h1")).getText();
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

    public static String[] get_numeric_lesson_info(WebDriver driver) {
        try {

            String video_info_text =
                    driver.findElement(By.cssSelector("#material-view > div.MaterialView.MaterialView-type" +
                            "--lecture > div.MaterialView-video > div > div > div.Header.material-lecture >" +
                            " div.Header-class > div.Header-class-title > span")).getText();
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

    public static int find_current_lecture_number(WebDriver driver) {
        int lesson_number = 0;
        try {
            String[] chapters_info = get_numeric_lesson_info(driver);
            lesson_number = Integer.parseInt(chapters_info[0]);
        } catch (Exception e) {
            System.out.println("\n FAIL TO GET THE CURRENT LESSON NUMBER");
        }
        return lesson_number;
    }

    public static void download_files(ArrayList<String> links, String parent_directory, String file_name) {
        try {
            for (String link : links) {
                URL download_url = new URL(link);
                String extension = new StringBuilder(link).substring(link.lastIndexOf("."));
                download_bytes(parent_directory, file_name + extension, download_url, printFormat);
            }
        } catch (Exception e) {
            System.out.format(printFormat, "SINGLE FILES LESSON FILE", "COULDN'T FIND ANY DOWNLOADABLE FILES FOR THIS LESSON \t");
        }
    }

    public static void download_files(String link, String parent_directory, String file_name) {
        try {
            URL download_url = new URL(link);
            String extension = new StringBuilder(link).substring(link.lastIndexOf("."));
            download_bytes(parent_directory, file_name + extension, download_url, printFormat);
        } catch (Exception e) {
            System.out.format(printFormat, "PACKED FILES LESSON FILE", "COULDN'T FIND ANY .ZIP DOWNLOADABLE FILE FOR THIS LESSON \t");
        }
    }

    public static void download_current_video_files(WebDriver driver, String file_name, String parent_directory) {
        WebElement download_all_files_button = null;
        List<WebElement> downloadable_files;
        ArrayList<String> links = new ArrayList<>();
        String link = "";

        try {
            download_all_files_button = driver.findElement(By.className("FilesTree-download"));
            link = download_all_files_button.getAttribute("href");
        } catch (NoSuchElementException | NullPointerException exception) {
            System.out.println("*couldn't find \"download all files\" button");
        }

        try {
            downloadable_files = driver.findElements(By.cssSelector(".FilesAndLinks-item"));
            for (WebElement element : downloadable_files) {
                links.add(element.getAttribute("href"));
            }
        } catch (NoSuchElementException | NullPointerException exception) {
            System.out.println("*couldn't find any downloadable file");
        }

        if (!link.isEmpty())
            download_files(link, parent_directory, file_name);
        else if (links.size() >= 1)
            download_files(links, parent_directory, file_name);

    }

    public static void save_lecture_html_content(WebDriver driver, String file_name, String course_directory, String extension, String css_selector) {
        try {

            String title = file_name;
/*
            WORKS BUT CHROMEDRIVER CRASHES
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String body = (String) js.executeScript("return document.querySelector('.MaterialLecture').innerHTML;");

            DOESN'T WORK RETURN NULL
            WebElement lecture_element = driver.findElement(By.cssSelector(".MaterialLecture"));
            String body = lecture_element.getAttribute("inner.HTML");

            try this:
 */
            WebElement element = driver.findElement(By.cssSelector(css_selector));
            String body = (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", element);

            if(body.isEmpty()) //abort if body content is empty
                return;

            String lecture_html = html_format.replace("$title", title);
            lecture_html = lecture_html.replace("$body", body);

            File newHtmlFile = new File(course_directory + File.separator + file_name + extension);
            FileUtils.writeStringToFile(newHtmlFile, lecture_html, (Charset) null);

            System.out.printf(printFormat, "DOWNLOADED FILE", newHtmlFile);
        } catch (IOException e) {
            System.out.println("couldn't write String to file");
            throw new RuntimeException(e);
        } catch (NullPointerException e1) {
            System.out.println("possibly JS executor return null");
            throw new RuntimeException(e1);
        }

    }


    public static boolean page_has_loaded(WebDriver driver, String old_text_value) {
        String current_text_value = find_current_video_title(driver);
        return !old_text_value.equals(current_text_value) && !old_text_value.isBlank();
    }

    public static boolean lecture_has_loaded(WebDriver driver, String old_text_value) {
        String current_text_value = find_current_lecture_title(driver);
        return !old_text_value.equals(current_text_value) && !old_text_value.isBlank();
    }

    public static void go_to_next_video_and_wait(WebDriver driver, String old_text_value) {
        try {
            WebElement next_video_button = driver.findElement(By.className("Header-course-actions-next"));
            next_video_button.click();
            while (!page_has_loaded(driver, old_text_value))
                Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println("\n FAIL TO WAIT UNTIL LOAD PAGE\n");
            throw new RuntimeException(e);
        }
    }

    public static void go_to_next_lesson_and_wait(WebDriver driver, String old_text_value) {
        try {
            WebElement next_video_button = driver.findElement(By.className("Header-course-actions-next"));
            next_video_button.click();
            while (!lecture_has_loaded(driver, old_text_value))
                Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println("\n FAIL TO WAIT UNTIL LOAD PAGE\n");
            throw new RuntimeException(e);
        }
    }

    public static void write_credentials(WebDriver driver, String email, String pass) {
        try {
            WebElement email_input = driver.findElement(By.name("email"));
            WebElement pass_input = driver.findElement(By.name("password"));

            WebElement submit_button = driver.findElement(By.cssSelector(".btn-Green.btn--md"));

            email_input.sendKeys(email);
            Thread.sleep(3000);

            pass_input.sendKeys(pass);
            Thread.sleep(3000);


            submit_button.click();
            Thread.sleep(20000);
        } catch (Exception e) {
            System.out.println("COULDN'T LOG IN AUTOMATICALLY");
            throw new RuntimeException(e);

        }
    }


}