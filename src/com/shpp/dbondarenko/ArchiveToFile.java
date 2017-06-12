package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        System.out.println("countByteOfTable " + countByteOfTable);
        byte[] bytesOfTable = new byte[countByteOfTable];
        System.arraycopy(bytesFromFile, 4, bytesOfTable, 0, countByteOfTable);
        System.out.println("bytesOfTable.length " + bytesOfTable.length);
        //System.out.println(bytesOfTable[0]);
        // System.out.println(bytesOfTable[188]);
        HashMap<String, Byte> encodingTable = restoreCodingTable(bytesOfTable);
        StringBuilder extraBits = toBinaryStringFromByte(bytesFromFile[3]);
        System.out.println(extraBits);
        byte[] bytesFromEncodedFile = new byte[bytesFromFile.length - bytesOfTable.length - 4];
        System.out.println("bytesFromEncodedFile " + bytesFromEncodedFile.length);
        System.arraycopy(bytesFromFile, 4 + countByteOfTable,
                bytesFromEncodedFile, 0, bytesFromEncodedFile.length);
        //System.out.println(Arrays.toString(bytesFromEncodedFile));
        byte[] bytesToFile = unarchiveFile(bytesFromEncodedFile, encodingTable, extraBits);
        System.out.println("bytesToFile " + bytesToFile.length);
        writeBytesToFile(bytesToFile, fileName);
    }

    private void writeBytesToFile(byte[] bytesToFile, String fileName) {
        System.out.println("bytesToFile: " + bytesToFile.length);
        /*for (byte b : bytesToFile) {
            System.out.println(b);
        }*/
        String name = createFileName(fileName);
        System.out.println(name);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(name);
            outputStream.write(bytesToFile);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Sorry. Create an archive failed.");
            e.printStackTrace();
        }
    }

    private String createFileName(String archiveName) {
        String fileName = archiveName.substring(0, archiveName.length() - FILE_EXTENSION.length());
        String[] nameAndExtension = fileName.split("\\.");
        return nameAndExtension[0] + "copy." + nameAndExtension[1];
    }

    private byte[] unarchiveFile(byte[] bytesFromEncodedFile, HashMap<String, Byte> encodingTable, StringBuilder extraBits) {

        StringBuilder bitSequence = createBitsLine(bytesFromEncodedFile, extraBits);
        System.out.println("bitSequence " + bitSequence);
        for (Map.Entry entry : encodingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        ArrayList<Byte> bytesList = new ArrayList<>();
        int bitSequenceLength = 0;
        while (bitSequenceLength < bitSequence.length()) {
            StringBuilder desiredBitSet = new StringBuilder();
            for (int i = bitSequenceLength; i < bitSequence.length(); i++) {
                desiredBitSet.append(bitSequence.charAt(i));
                if (encodingTable.containsKey(String.valueOf(desiredBitSet))) {
                    bytesList.add(encodingTable.get(String.valueOf(desiredBitSet)));
                    bitSequenceLength = i + 1;
                    //bitSequence.delete(0, i + 1);
                    break;
                }
            }
        }
        System.out.println(bytesList.size());
        return fromListToArray(bytesList);
    }

    private byte[] fromListToArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    private StringBuilder createBitsLine(byte[] bytesFromEncodedFile, StringBuilder extraBits) {
        System.out.println("bytesFromEncodedFile.lenght: " + bytesFromEncodedFile.length);
        StringBuilder bitSequence = new StringBuilder();
        for (byte oneByte : bytesFromEncodedFile) {
            StringBuilder bitsFromByte = toBinaryStringFromByte(oneByte);
            System.out.println("bitsFromByte " + bitsFromByte);
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
            byte firstByte = bytesOfTable[i];
            // System.out.print(firstByte+": ");
            StringBuilder secondByte = toBinaryStringFromByte(bytesOfTable[i + 1]);
            StringBuilder thirdByte = toBinaryStringFromByte(bytesOfTable[i + 2]);
            String idByte = removeLeadingZeros(secondByte.append(thirdByte));
            // System.out.println(idByte);
            idByte = idByte.substring(1);
            //System.out.println(idByte);
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
