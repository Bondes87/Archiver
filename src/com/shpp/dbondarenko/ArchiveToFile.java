package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * File: ArchiveToFile.java
 * Created by Dmitro Bondarenko on 06.06.2017.
 */
public class ArchiveToFile {
    private static final String FILE_EXTENSION = "-bds";

    public void unarchive(String fileName) {
        byte[] bytesFromFile = readFileToBytes(fileName);
        for (byte oneByte : bytesFromFile) {
            System.out.println(oneByte);
        }
        int countByteOfTable = getCountByteOfTable(bytesFromFile);
        System.out.println(countByteOfTable);
        byte[] bytesOfTable = new byte[countByteOfTable];
        System.arraycopy(bytesFromFile, 4, bytesOfTable, 0, countByteOfTable);
        System.out.println(bytesOfTable.length);
        System.out.println(bytesOfTable[0]);
        System.out.println(bytesOfTable[188]);
        HashMap<String, Byte> codingTable = restoreCodingTable(bytesOfTable);
        /*StringBuilder extraBits = toBinaryStringFromByte(bytesFromFile[3]);
        System.out.println(extraBits);*/
    }


    private int getCountByteOfTable(byte[] bytesFromFile) {
        StringBuilder firstByte = toBinaryStringFromByte(bytesFromFile[0]);
        System.out.println(firstByte);
       /* String firstByte = Integer.toBinaryString(bytesFromFile[0]);
        System.out.println(firstByte);*/
        StringBuilder secondByte = toBinaryStringFromByte(bytesFromFile[1]);
        System.out.println(secondByte);
       /* String secondByte = Integer.toBinaryString(bytesFromFile[0]);
        System.out.println(secondByte);*/
        return Integer.parseInt(String.valueOf(firstByte.append(secondByte)), 2);
    }

    private StringBuilder toBinaryStringFromByte(byte oneByte) {
        StringBuilder firstByte = new StringBuilder();
        firstByte.append(Integer.toBinaryString(oneByte & 255 | 256).substring(1));
        String s = Integer.toBinaryString(oneByte);
        return firstByte;
    }

    private byte[] readFileToBytes(String fileName) {
        byte[] bytesFromFile = new byte[0];
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            bytesFromFile = new byte[inputStream.available()];
            inputStream.read(bytesFromFile);
        } catch (IOException e) {
            System.out.println("Sorry. Such file was not found.");
            e.printStackTrace();
        }
        return bytesFromFile;
    }
}
