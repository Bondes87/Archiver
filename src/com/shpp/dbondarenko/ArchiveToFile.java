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
        System.out.println("bytesFromFile " + bytesFromFile.length);
        for (byte oneByte : bytesFromFile) {
            System.out.println(oneByte);
        }
        int countByteOfTable = getCountByteOfTable(bytesFromFile);
        System.out.println(countByteOfTable);
        byte[] bytesOfTable = new byte[countByteOfTable];
        System.arraycopy(bytesFromFile, 4, bytesOfTable, 0, countByteOfTable);
        //System.out.println(bytesOfTable.length);
        //System.out.println(bytesOfTable[0]);
        // System.out.println(bytesOfTable[188]);
        HashMap<String, Byte> encodingTable = restoreCodingTable(bytesOfTable);
        StringBuilder extraBits = toBinaryStringFromByte(bytesFromFile[3]);
        byte[] bytesFromEncodedFile = new byte[bytesFromFile.length - countByteOfTable - 4];
        System.arraycopy(bytesFromFile, 4 + countByteOfTable,
                bytesFromEncodedFile, 0, bytesFromFile.length - countByteOfTable - 4);
        byte[] bytesToFile = unarchiveFile(bytesFromEncodedFile, encodingTable, extraBits);
        //System.out.println("bytesFromEncodedFile " + bytesFromEncodedFile.length);
        // System.out.println(bytesFromEncodedFile[bytesFromEncodedFile.length-1]);
        /*
        System.out.println(extraBits);*/
    }

    private byte[] unarchiveFile(byte[] bytesFromEncodedFile, HashMap<String, Byte> encodingTable, StringBuilder extraBits) {
        StringBuilder bitSequence = createBitsLine(bytesFromEncodedFile, extraBits);
        return new byte[0];
    }

    private StringBuilder createBitsLine(byte[] bytesFromEncodedFile, StringBuilder extraBits) {
        StringBuilder bitSequence = new StringBuilder();
        for (byte oneByte : bytesFromEncodedFile) {
            StringBuilder bitsFromByte = toBinaryStringFromByte(oneByte);
            System.out.println(bitsFromByte);
            bitSequence.append(bitsFromByte);
        }
        System.out.println(bitSequence);
        System.out.println(bitSequence.length());
        bitSequence.delete(bitSequence.length() - removeLeadingZeros(extraBits).length(),
                bitSequence.length());
        System.out.println(bitSequence);
        System.out.println(bitSequence.length());
        return bitSequence;
    }

    private HashMap<String, Byte> restoreCodingTable(byte[] bytesOfTable) {
        HashMap<String, Byte> huffmanReverseTable = new HashMap<>();
        for (int j = 0, i = 0; i < bytesOfTable.length; j++, i = j * 3) {
            byte firstByte = bytesOfTable[j];
            StringBuilder secondByte = toBinaryStringFromByte(bytesOfTable[j + 1]);
            StringBuilder thirdByte = toBinaryStringFromByte(bytesOfTable[j + 2]);
            String idByte = removeLeadingZeros(secondByte.append(thirdByte));
            huffmanReverseTable.put(idByte, firstByte);
        }
        //System.out.println("size:" + huffmanReverseTable.size());
        return huffmanReverseTable;
    }

    private String removeLeadingZeros(StringBuilder line) {
        int number = Integer.parseInt(String.valueOf(line), 2);
        //System.out.println(number);
        return Integer.toBinaryString(number);
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
