package com.shpp.dbondarenko;

import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File: ZipArchive.java
 * Created by Dmitro Bondarenko on 18.06.2017.
 */
public class ZipArchive {
    ZipArchive() {
        fileToZip("01.MOV");
    }

    public static void main(String[] args) {
        new ZipArchive();
    }

    public void fileToZip(String fileName) {
        try {
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread ReaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileInputStream fileInputStream;
                    try {
                        System.out.println("Please wait.");
                        fileInputStream = new FileInputStream(fileName);
                        byte[] buffer = new byte[1024];
                        int bufferSize = fileInputStream.read(buffer);
                        while (bufferSize != -1) {
                            byte[] bytesToRead = Arrays.copyOfRange(buffer, 0, bufferSize);
                            pipedOutputStream.write(bytesToRead);
                            bufferSize = fileInputStream.read(buffer);
                        }
                        fileInputStream.close();
                        pipedOutputStream.close();
                    } catch (IOException e) {
                        System.out.println("Error");
                        e.printStackTrace();
                    }
                }
            });
            Thread ZipWriterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ZipOutputStream zipOutputStream;
                    try {
                        zipOutputStream = new ZipOutputStream(new FileOutputStream(fileName + ".zip"));
                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipOutputStream.putNextEntry(zipEntry);
                        byte[] buffer = new byte[1024];
                        int bufferSize = pipedInputStream.read(buffer);
                        while (bufferSize != -1) {
                            byte[] bytesToWrite = Arrays.copyOfRange(buffer, 0, bufferSize);
                            zipOutputStream.write(bytesToWrite);
                            bufferSize = pipedInputStream.read(buffer);
                        }
                        zipOutputStream.closeEntry();
                        zipOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            ReaderThread.start();
            ZipWriterThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
