package pruebas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloaderHelper {
    public static void validate_file(String root, String name) {
        File root1 = new File(root);
        File file = new File(root + File.separator + name);
        //VERIFY PRIVILEGES TO WRITE
        if (!root1.canWrite())
            throw new RuntimeException("THE PROGRAM DOESN'T HAVE PRIVILEGES TO WRITE IN THIS DIRECTORY");

        //HANDLE ROOT DETECTION/CREATION
        if (!root1.exists()) {
            root1.mkdirs();
            if (root1.exists())
                System.out.println("THE ROOT DIRECTORY WAS CREATED " + root1);
            else
                throw new RuntimeException("COULDN'T CREATE THE ROOT DIRECTORY " + root1);
        } else
            System.out.println("THE ROOT DIRECTORY ALREADY EXISTS " + root1);

        //HANDLE FILE CREATION/DETECTION
        if (!file.exists())
            System.out.println("EVERYTHING IS OKAY TO WRITE THE FILE " + file);
        else
            System.out.println("THE FILE ALREADY EXISTS");
    }

    public static void download_bytes(String root_file, String file_name, URL url) {
        File file_to_download = new File(root_file + File.separator + file_name);
        System.out.println("DOWNLOADING FILE FROM THE URL " + url + " ruta-> " +file_to_download.getPath());
        validate_file(root_file, file_name);
        try {//DOWNLOAD THE FILE
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            OutputStream out = new FileOutputStream(file_to_download);
            int b = 0;
            while (b != -1) {
                b = in.read();
                if (b != -1)
                    out.write(b);
            }
            out.close();
            in.close();
            System.out.println("FILE DOWNLOADED ->> " + file_to_download);
        } catch (Exception e) {
            System.out.println("FAIL TO DOWNLOAD THE FILE FROM THE URL " + url + " FILE: " + file_to_download);
            throw new RuntimeException(e);
        }
    }

}
