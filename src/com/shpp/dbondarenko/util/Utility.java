package com.shpp.dbondarenko.util;

import java.io.*;
import java.util.Arrays;

/**
 * File: Utility.java
 * The class in which are the common methods necessary for archiving and unarchiving, as well as constants.
 * Created by Dmitro Bondarenko on 16.06.2017.
 */
public class Utility {
    // The constant controlling the buffer size.
    public static final int BUFFER_SIZE_FOR_READING_AND_WRITING = 1024;
    // The constant indicating the number of bits in the byte.
    public static final int COUNT_OF_BITS_IN_BYTE = 8;
    // The constant indicating a binary system of calculi.
    public static final int BINARY_SYSTEM = 2;
    // The constant that informs that the archive is created.
    public static final String MESSAGE_ARCHIVE_CREATED = "Archive created: ";
    // The constant that informs that an error occurred while creating the archive.
    public static final String MESSAGE_CREATE_AN_ARCHIVE_FAILED = "Sorry. Create an archive failed.";
    // The constant which is responsible for additional expansion of the archive.
    public static final String ADDITIONAL_ARCHIVE_EXTENSION = "-bds";
    // The constant which informs that it is necessary to wait.
    public static final String MESSAGE_PLEASE_WAIT = "Please wait!!!";
    // The constant that informs that the file is created.
    public static final String MESSAGE_FILE_CREATED = "File created: ";
    // The constant that informs that an error occurred while the file was being restored.
    public static final String MESSAGE_FILE_COULD_NOT_BE_RESTORED = "Sorry. The file could not be restored";
    // The constant that informs that the file was not found.
    public static final String MESSAGE_FILE_NOT_FOUND = "Sorry. Such file was not found.";
    // The constant that is responsible for ending the name of the new file.
    public static final String ENDING_NAME_OF_NEW_FILE = "_copy.";
    // The constant responsible for expanding the archive.
    public static final String ZIP_FILE_EXTENSION = ".zip";

    /**
     * Reads the bytes from the file and passes them to another pipeline stream.
     *
     * @param fileName The name of the file from which the archive is created.
     * @param output   The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     */
    public static void readFile(String fileName, PipedOutputStream output) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = fileInputStream.read(buffer);
            while (bufferSize != -1) {
                byte[] bytesToRead = Arrays.copyOfRange(buffer, 0, bufferSize);
                output.write(bytesToRead);
                bufferSize = fileInputStream.read(buffer);
            }
            fileInputStream.close();
            output.close();
        } catch (IOException e) {
            System.out.println(MESSAGE_FILE_NOT_FOUND);
            e.printStackTrace();
        }
    }

    /**
     * Writes data to a file. To operate the method, you need the conveyor stream PipedOutputStream,
     * which sends the data for the conveyor stream PipedInputStream.
     *
     * @param fileName         The name of the file to which the data will be written.
     * @param pipedInputStream The pipeline flow for reading data received from PipedOutputStream.
     * @param resultMessage    The message that will be displayed if the write process was successful.
     * @param errorMessage     The message that will be displayed if the write process failed.
     */
    public static void writeFile(String fileName, PipedInputStream pipedInputStream, String resultMessage, String errorMessage) {
        FileOutputStream fileOutputStream;
        try {
            File archive = new File(fileName);
            if (archive.exists()) {
                archive.delete();
            }
            archive.createNewFile();
            fileOutputStream = new FileOutputStream(archive, true);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = pipedInputStream.read(buffer);
            while (bufferSize != -1) {
                byte[] bytesToWrite = Arrays.copyOfRange(buffer, 0, bufferSize);
                fileOutputStream.write(bytesToWrite);
                bufferSize = pipedInputStream.read(buffer);
            }
            fileOutputStream.close();
            pipedInputStream.close();
            System.out.println(resultMessage + fileName);
        } catch (IOException e) {
            System.out.println(errorMessage);
            e.printStackTrace();
        }
    }

    /**
     * Checks if the specified file exists.
     *
     * @param fileName The name of the file.
     */
    public boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }
}
