package com.shpp.dbondarenko.zip;

import com.shpp.dbondarenko.util.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * File: ZipToFile.java
 * Created by Dmitro Bondarenko on 19.06.2017.
 */
public class ZipToFile extends Utility {
    /**
     * Restore file from zip archive.
     *
     * @param zipName The name of the zip archive from which to restore the file.
     */
    public void restoreFileFromZip(String zipName) {
        try {
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread zipReaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    readZip(zipName, pipedOutputStream);
                }
            });
            Thread writerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeFile(createFileName(zipName), pipedInputStream,
                            MESSAGE_FILE_CREATED, MESSAGE_FILE_COULD_NOT_BE_RESTORED);
                }
            });
            zipReaderThread.start();
            writerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the bytes from the zip archive and passes them to another pipeline stream.
     *
     * @param zipName           The name of the zip archive from which the file is restored.
     * @param pipedOutputStream The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     */
    private void readZip(String zipName, PipedOutputStream pipedOutputStream) {
        ZipInputStream zipInputStream;
        try {
            System.out.println(MESSAGE_PLEASE_WAIT);
            zipInputStream = new ZipInputStream(new FileInputStream(zipName));
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry != null) {
                byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
                int bufferSize = zipInputStream.read(buffer);
                while (bufferSize != -1) {
                    byte[] bytesToRead = Arrays.copyOfRange(buffer, 0, bufferSize);
                    pipedOutputStream.write(bytesToRead);
                    bufferSize = zipInputStream.read(buffer);
                }
            }
            zipInputStream.close();
            pipedOutputStream.close();
        } catch (IOException e) {
            System.out.println(MESSAGE_FILE_NOT_FOUND);
            e.printStackTrace();
        }
    }

    /**
     * Create a new file name.
     *
     * @param zipName The name of the zip archive from which the file is restored.
     * @return The new file name.
     */
    private String createFileName(String zipName) {
        String[] nameAndExtension = zipName.split("\\.");
        return nameAndExtension[0] + "_copy." + nameAndExtension[1];
    }
}
