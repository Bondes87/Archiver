package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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
        HashMap<Byte, Integer> bytesFrequencyOfFile = countBytesFrequency(bytesFromFile);
        int count = 0;
        for (Map.Entry<Byte, Integer> entry : bytesFrequencyOfFile.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            count += entry.getValue();
        }
       /* System.out.println(bytesFrequencyOfFile.size());
        System.out.println(count);*/
        HashMap<Byte, Integer> sortedBytesFrequency = sortByValue(bytesFrequencyOfFile);
        for (Map.Entry<Byte, Integer> entry : sortedBytesFrequency.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private HashMap<Byte, Integer> countBytesFrequency(byte[] bytesFromFile) {
        HashMap<Byte, Integer> bytesFrequencyOfFile = new HashMap<>();
        for (byte oneByte : bytesFromFile) {
            if (bytesFrequencyOfFile.containsKey(oneByte)) {
                bytesFrequencyOfFile.put(oneByte, bytesFrequencyOfFile.get(oneByte) + 1);
            } else {
                bytesFrequencyOfFile.put(oneByte, 1);
            }
        }
        return bytesFrequencyOfFile;
    }

    private HashMap<Byte, Integer> sortByValue(HashMap<Byte, Integer> bytesFrequencyOfFile) {
        HashMap<Byte, Integer> sortedBytesFrequency = new LinkedHashMap<>();
        List<Map.Entry<Byte, Integer>> list = new ArrayList<>(bytesFrequencyOfFile.entrySet());
        list.sort(new Comparator<Map.Entry<Byte, Integer>>() {
            @Override
            public int compare(Map.Entry<Byte, Integer> o1,
                               Map.Entry<Byte, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        for (Map.Entry<Byte, Integer> entry : list) {
            sortedBytesFrequency.put(entry.getKey(), entry.getValue());
        }
        return sortedBytesFrequency;
    }

    private byte[] readFileToBytes(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] bytesFromFile = new byte[inputStream.available()];
        inputStream.read(bytesFromFile);
        return bytesFromFile;
    }
}
