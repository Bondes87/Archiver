package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * File: com.shpp.dbondarenko.Archiver.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Archiver {
    public void createArchive(String fileName) throws IOException {
        byte[] bytesFromFile = readFileToBytes(fileName);
        fileArchiving(bytesFromFile);
        System.out.println(bytesFromFile.length);
    }

    private void fileArchiving(byte[] bytesFromFile) {
        HashMap<Byte, Integer> countBytesFrequencyOfFile = countBytesFrequency(bytesFromFile);
        int count=0;
        for (Map.Entry<Byte, Integer> entry : countBytesFrequencyOfFile.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            count+=entry.getValue();
        }
        System.out.println(countBytesFrequencyOfFile.size());
        System.out.println(count);

    }

    private HashMap<Byte, Integer> countBytesFrequency(byte[] bytesFromFile) {
        HashMap<Byte, Integer> countBytesFrequencyOfFile = new HashMap<>();
        for (byte oneByte : bytesFromFile) {
            if (countBytesFrequencyOfFile.containsKey(oneByte)) {
                countBytesFrequencyOfFile.put(oneByte, countBytesFrequencyOfFile.get(oneByte) + 1);
            } else {
                countBytesFrequencyOfFile.put(oneByte, 1);
            }
        }
        return countBytesFrequencyOfFile;
    }

    private byte[] readFileToBytes(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] bytesFromFile = new byte[inputStream.available()];
        inputStream.read(bytesFromFile);
        return bytesFromFile;
    }
}
