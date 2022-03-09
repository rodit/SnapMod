package xyz.rodit.snapmod.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static void copyTo(InputStream source, File dest) {
        try (OutputStream out = new FileOutputStream(dest)) {
            copyTo(source, out);
        } catch (IOException e) {
            System.err.println("Error while copying stream to file " + dest + ". " + e.getMessage());
        }
    }

    public static void copyTo(InputStream source, OutputStream dest) throws IOException {
        copyTo(source, dest, DEFAULT_BUFFER_SIZE);
    }

    public static void copyTo(InputStream source, OutputStream dest, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = source.read(buffer, 0, DEFAULT_BUFFER_SIZE)) > -1) {
            dest.write(buffer, 0, read);
        }
    }
}
