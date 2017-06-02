package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * File: com.shpp.dbondarenko.Archiver.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Archiver {
    public void createArchive(String fileName) throws IOException {
        byte[] bytesFromFile = readFileToBytes(fileName);
        System.out.println(bytesFromFile.length);
    }

    private byte[] readFileToBytes(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] bytesFromFile = new byte[inputStream.available()];
        inputStream.read(bytesFromFile);
        return bytesFromFile;
    }
}
