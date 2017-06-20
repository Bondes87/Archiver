package com.shpp.dbondarenko.archiver.zip;

import com.shpp.dbondarenko.util.Utility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File: FileToZip.java
 * Class in which an zip archive is created from the file.
 * Created by Dmitro Bondarenko on 18.06.2017.
 */
public class FileToZip extends Utility {

    /**
     * Create an zip archive from a file.
     *
     * @param fileName The name of the file from which the zip archive is created.
     */
    public void createZipFromFile(String fileName) {
        if (isFileExist(fileName)) {
            try {
                final PipedOutputStream pipedOutputStream = new PipedOutputStream();
                final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                Thread readerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(MESSAGE_PLEASE_WAIT);
                        readFile(fileName, pipedOutputStream);
                    }
                });
                Thread zipWriterThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        writeToZip(createZipName(fileName), fileName, pipedInputStream);
                    }
                });
                readerThread.start();
                zipWriterThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(MESSAGE_FILE_NOT_FOUND);
        }
    }

    /**
     * Writes data to a zip archive. To operate the method, you need the conveyor stream PipedOutputStream,
     * which sends the data for the conveyor stream PipedInputStream.
     *
     * @param zipName          The name of the zip archive to which the data will be written.
     * @param fileName         The name of the file to be archived.
     * @param pipedInputStream The pipeline flow for reading data received from PipedOutputStream.
     */
    private void writeToZip(String zipName, String fileName, PipedInputStream pipedInputStream) {
        ZipOutputStream zipOutputStream;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipName));
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = pipedInputStream.read(buffer);
            while (bufferSize != -1) {
                byte[] bytesToWrite = Arrays.copyOfRange(buffer, 0, bufferSize);
                zipOutputStream.write(bytesToWrite);
                bufferSize = pipedInputStream.read(buffer);
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            System.out.println(MESSAGE_ARCHIVE_CREATED + zipName);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Create a zip archive name.
     *
     * @param fileName The name of the file from which the zip archive is created.
     * @return The zip archive name.
     */
    private String createZipName(String fileName) {
        return fileName + ZIP_FILE_EXTENSION;
    }
}
