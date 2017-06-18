package com.shpp.dbondarenko;

import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * File: ZipArchive.java
 * Created by Dmitro Bondarenko on 18.06.2017.
 */
public class ZipArchive {
    ZipArchive() {
        zipToFile("01.MOV.zip");
    }

    public static void main(String[] args) {
        new ZipArchive();
    }

    public void fileToZip(String fileName) {
        try {
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread readerThread = new Thread(new Runnable() {
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
            Thread zipWriterThread = new Thread(new Runnable() {
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
            readerThread.start();
            zipWriterThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void zipToFile(String zipName) {
        try {
            String[] fileName = new String[1];
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread zipReaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ZipInputStream zipInputStream;
                    try {
                        System.out.println("Please wait.");
                        zipInputStream = new ZipInputStream(new FileInputStream(zipName));
                        ZipEntry entry;
                        if ((entry = zipInputStream.getNextEntry()) != null) {
                            fileName[0] = entry.getName();
                            String[] nameAndExtension = fileName[0].split("\\.");
                            fileName[0] = nameAndExtension[0] + "_copy." + nameAndExtension[1];
                            byte[] buffer = new byte[1024];
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
                        System.out.println("Error read");
                        e.printStackTrace();
                    }
                }
            });

            Thread writerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fileOutputStream;
                    try {
                        File archive = new File(fileName[0]);
                        if (archive.exists()) {
                            archive.delete();
                        }
                        archive.createNewFile();
                        fileOutputStream = new FileOutputStream(archive, true);
                        byte[] buffer = new byte[1024];
                        int bufferSize = pipedInputStream.read(buffer);
                        while (bufferSize != -1) {
                            byte[] bytesToWrite = Arrays.copyOfRange(buffer, 0, bufferSize);
                            fileOutputStream.write(bytesToWrite);
                            bufferSize = pipedInputStream.read(buffer);
                        }
                        fileOutputStream.close();
                        pipedInputStream.close();
                        System.out.println("Ok" + fileName);
                    } catch (IOException e) {
                        System.out.println("Error write");
                        e.printStackTrace();
                    }
                }
            });
            zipReaderThread.start();
            writerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
