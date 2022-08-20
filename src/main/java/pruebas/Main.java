package pruebas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;
import java.io.File;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Scanner;

import static pruebas.InvisibleChromeDriver.*;

public class Main {
    public static void main(String[] args) {
        Scanner user_input = new Scanner(System.in);


        final String printFormat = "%-50s || %-30s \n";
        final String numeration_separator = " ";
        final String platzi_login_url = "https://platzi.com/login";

        System.out.printf(printFormat, "", "BIENVENIDO A PLATZI DOWNLOADER\n");
        System.out.print("COMPLETA LOS SIGUIENTES DATOS\n");

        System.out.print("Url del primer video a descargar (https://ejemplo.com):\t");
        final String first_video_url = user_input.next();
        System.out.printf(printFormat, "INITIAL LESSON URL", first_video_url);

        System.out.print("correo cuenta platzi:\t");
        final String mail = user_input.next();
        System.out.format(printFormat, "EMAIL", mail);

        System.out.print("password cuenta platzi:\t");
        final String password = user_input.next();
        System.out.format(printFormat, "PASSWORD", password);

        System.out.print("Raiz para guardar el curso (comilla al final):\t");

        user_input.useDelimiter("\n");

        String root = "";
        String token = user_input.next("\"[^\"]+\"");
        root = token.replaceAll("\"", "");
        System.out.println(root);

        final File parent_directory = new File(root);
        System.out.format(printFormat, "ROOT DIRECTORY", parent_directory);

        JOptionPane.showMessageDialog(null, "Para poder comenzar solo necesitas iniciar sesión una vez para que el programa obtenga" +
                " acceso a los videos, \nOJO: SI TE SALTA UN CAPCHA  TIENES 20 SEGUNDOS PARA RESOLVERLO\n ** Cierra esta ventana para comenzar. \n Si ocurre algún error favor de reportar a @Brdn08", "ESPERA UN POCO...", JOptionPane.WARNING_MESSAGE);

        WebDriver driver = new ChromeDriver(create_invisible_chrome_options());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        driver.get(platzi_login_url);

        write_credentials(driver, mail, password);

        driver.get(first_video_url);

        final String course_name = get_current_course(driver);
        System.out.format(printFormat, "COURSE NAME", course_name);


        final File course_directory = new File(parent_directory + File.separator + course_name);
        final File lesson_resources_directory = new File(course_directory + File.separator + "archivos de las clases");


        final int total_videos_number = find_number_of_videos(driver);
        System.out.format(printFormat, "NUMBER OF VIDEOS FOR THIS COURSE: ", total_videos_number);

        String current_video_title = "Default title";
        int current_video_number = 1;
        String current_files_download_name = "Default files download name";
        String current_stream_url = "Default stream url";
        String current_video_download_name = "Default video download name";
        System.out.println("-------------------------------------------------------------------------------------------------");

        do {
            System.out.println("_________________________________________________________________________________________________\n");
            try {
                if (driver.findElement(By.cssSelector("div.Header-lecture")) != null) {

                    System.out.format(printFormat, "LECTURE TYPE", "LECTURA");

                    int number = find_current_lecture_number(driver);
                    System.out.format(printFormat, "LECTURE NUMBER", number);

                    String name = find_current_lecture_title(driver);
                    System.out.format(printFormat, "LECTURE TITLE", name);

                    try {
                        String current_lecture_download_name = number + numeration_separator + name;

                        download_current_lesson(driver, current_lecture_download_name,
                                course_directory.toString().replaceAll(":", ""), printFormat, ".png");
                    } catch (Exception e) {
                        System.out.println("ATTENTION: COULDN'T TAKE SCREENSHOT FOR THIS LESSON\t");
                    }

                    go_to_next_lesson_and_wait(driver, name);
                    continue;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.format(printFormat, "LESSON TYPE", "VIDEO");

            current_video_number = find_current_video_number(driver);
            System.out.format(printFormat, "LESSON NUMBER", current_video_number);

            current_video_title = Normalizer.normalize(find_current_video_title(driver), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^a-zA-Z\\d ]", "");

            System.out.format(printFormat, "LESSON TITLE", current_video_title);


            try {
                current_files_download_name = current_video_number + numeration_separator + current_video_title
                        + " - " + driver.findElement(By.className("FilesAndLinks-title")).getText();
                download_current_video_files(driver, current_files_download_name, lesson_resources_directory.toString(), printFormat);
            } catch (Exception e) {
                System.out.println("ATTENTION: COULDN'T FIND/DOWNLOAD FILES FOR THIS LESSON\t");
            }

            current_stream_url = get_current_stream_url(driver);
            System.out.format(printFormat, "STREAMING URL", current_stream_url);


            if (!course_directory.exists())
                course_directory.mkdirs();


            current_video_download_name = current_video_number + numeration_separator + current_video_title + ".mp4";
            System.out.format(printFormat, "DOWNLOADING VIDEO FILE PLEASE WAIT ...", current_video_download_name);

            download_stream(current_stream_url, course_directory + File.separator + current_video_download_name);
            System.out.println(current_video_download_name + " WAS SUCCESSFULLY DOWNLOADED \n");

            go_to_next_video_and_wait(driver, current_video_title);

        } while (current_video_number < total_videos_number);

        System.out.println("ALL THE VIDEOS FOR THIS COURSE WAS SUCCESSFULLY DOWNLOADED");
        System.out.println(current_video_number + "/" + total_videos_number + " WERE DOWNLOADED FROM THE COURSE ->> " + course_name);
        System.out.println("ALL THE FILES ARE IN THE FOLLOWING PATH" + course_directory);
        //binig23431@kaseig.com contra12
    }
}
