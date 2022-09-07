package pruebas;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static pruebas.InvisibleChromeDriver.printFormat;

public class FfmpegExtractor {

    public static String get_temporary_ffmpeg_path() throws URISyntaxException, IOException {
        final URI uri = getJarURI();
        final URI exe = getFile(uri, "ffmpeg.exe");
        System.out.printf(printFormat, "TEMPORARY FFMPEG FILE", exe);
        return exe.getPath().replaceFirst("/", "");
    }

    private static URI getJarURI() throws URISyntaxException {
        final ProtectionDomain domain;
        final CodeSource source;
        final URL url;
        final URI uri;

        domain = Main.class.getProtectionDomain();
        source = domain.getCodeSource();
        url = source.getLocation();
        uri = url.toURI();

        return (uri);
    }

    private static URI getFile(final URI where, final String fileName) throws ZipException, IOException {
        final File location = new File(where);
        final URI fileURI;


        // not in a JAR, just return the path on disk
        if (location.isDirectory())
            fileURI = URI.create(where + fileName);
         else {
            final ZipFile zipFile = new ZipFile(location);
            try {
                fileURI = extract(zipFile, fileName);
            } finally {
                zipFile.close();
            }
        }

        return (fileURI);
    }

    private static URI extract(final ZipFile zipFile, final String fileName) throws IOException {

        final ZipEntry entry = zipFile.getEntry(fileName);
        if (entry == null)
            throw new FileNotFoundException("cannot find file: " + fileName + " in archive: " + zipFile.getName());


        final File tempFile = File.createTempFile(fileName, ".exe");
        tempFile.deleteOnExit();//elimina el archivo al salir

        final InputStream zipStream = zipFile.getInputStream(entry);
        OutputStream fileStream = null;

        try {
            fileStream = new FileOutputStream(tempFile);

            final byte[] buf = new byte[1024];
            int i = 0;

            while ((i = zipStream.read(buf)) != -1)
                fileStream.write(buf, 0, i);


        } finally {
            close(zipStream);
            close(fileStream);
        }

        return (tempFile.toURI());
    }

    private static void close(final Closeable stream) {
        if (stream != null)
            try {
                stream.close();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }

    }

}
