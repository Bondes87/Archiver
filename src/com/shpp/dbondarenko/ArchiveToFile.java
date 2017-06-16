package com.shpp.dbondarenko;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * File: ArchiveToFile.java
 * Class in which the file from the archive is restored.
 * Created by Dmitro Bondarenko on 06.06.2017.
 */
public class ArchiveToFile {
    private static final int BUFFER_SIZE_FOR_READING_AND_WRITING = 1024;
    private static final int BINARY_SYSTEM = 2;
    private static final String ADDITIONAL_ARCHIVE_EXTENSION = "-bds";
    private static final String MESSAGE_PLEASE_WAIT = "Please wait!!!";
    private static final String MESSAGE_FILE_CREATED = "File created: ";
    private static final String MESSAGE_FILE_COULD_NOT_BE_RESTORED = "Sorry. The file could not be restored";
    private static final String MESSAGE_FILE_NOT_FOUND = "Sorry. Such file was not found.";

    private HashMap<String, Byte> decodingTable;

    public void restoreFileFromArchive(String fileName) {
        try {
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread ReaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileInputStream fileInputStream;
                    try {
                        System.out.println(MESSAGE_PLEASE_WAIT);
                        fileInputStream = new FileInputStream(fileName);
                        int countByteOfTable = getCountByteOfTable(fileInputStream);
                        restoreCodingTable(fileInputStream, countByteOfTable);
                        decodeArchive(fileInputStream, pipedOutputStream);
                        fileInputStream.close();
                        pipedOutputStream.close();
                    } catch (IOException e) {
                        System.out.println(MESSAGE_FILE_NOT_FOUND);
                        e.printStackTrace();
                    }
                }
            });
            Thread WriterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeFile(createFileName(fileName), pipedInputStream);
                }
            });
            ReaderThread.start();
            WriterThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCountByteOfTable(FileInputStream fileInputStream) throws IOException {
        byte[] buffer = new byte[2];
        int bufferSize = fileInputStream.read(buffer);
        int countByteOfTable = 0;
        if (bufferSize != -1) {
            StringBuilder firstByte = toBinaryStringFromByte(buffer[0]);
            StringBuilder secondByte = toBinaryStringFromByte(buffer[1]);
            countByteOfTable = Integer.parseInt(String.valueOf(firstByte.append(secondByte)), BINARY_SYSTEM);
        }
        return countByteOfTable;
    }

    private void restoreCodingTable(FileInputStream fileInputStream, int countByteOfTable) throws IOException {
        decodingTable = new HashMap<>();
        byte[] buffer = new byte[countByteOfTable];
        int bufferSize = fileInputStream.read(buffer);
        if (bufferSize != -1) {
            for (int j = 0, i = 0; i < buffer.length; j++, i = j * 3) {
                byte firstByte = buffer[i];
                StringBuilder secondByte = toBinaryStringFromByte(buffer[i + 1]);
                StringBuilder thirdByte = toBinaryStringFromByte(buffer[i + 2]);
                String idByte = removeLeadingZeros(secondByte.append(thirdByte));
                idByte = idByte.substring(1);
                decodingTable.put(idByte, firstByte);
            }
        }
    }

    private StringBuilder toBinaryStringFromByte(byte oneByte) {
        StringBuilder firstByte = new StringBuilder();
        firstByte.append(Integer.toBinaryString(oneByte & 255 | 256).substring(1));
        return firstByte;
    }

    private String removeLeadingZeros(StringBuilder line) {
        int number = Integer.parseInt(String.valueOf(line), BINARY_SYSTEM);
        return Integer.toBinaryString(number);
    }

    private void decodeArchive(FileInputStream fileInputStream, PipedOutputStream pipedOutputStream) throws IOException {
        StringBuilder bitSequence = new StringBuilder();
        byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
        int bufferSize = fileInputStream.read(buffer);
        String bitsResidue = null;
        while (bufferSize != -2) {
            byte[] bytesToDecode = Arrays.copyOf(buffer, bufferSize);
            if ((bufferSize = fileInputStream.read(buffer)) != -1) {
                createBitSequence(bitSequence, bitsResidue, bytesToDecode, bytesToDecode.length);
                bitsResidue = restoreByte(pipedOutputStream, bitSequence);
            } else {
                String endCode = getBinaryStringFromEndByte(bytesToDecode);
                createBitSequence(bitSequence, bitsResidue, bytesToDecode, bytesToDecode.length - 2);
                bitSequence.append(endCode);
                bitsResidue = restoreByte(pipedOutputStream, bitSequence);
                bufferSize = -2;
            }
        }
    }

    private void createBitSequence(StringBuilder bitSequence, String bitsResidue, byte[] bytesToDecode,
                                   int iterationsCount) {
        if (bitsResidue != null) {
            bitSequence.append(bitsResidue);
        }
        for (int i = 0; i < iterationsCount; i++) {
            byte oneByte = bytesToDecode[i];
            bitSequence.append(toBinaryStringFromByte(oneByte));
        }
    }

    private String restoreByte(PipedOutputStream pipedOutputStream, StringBuilder bitSequence) throws IOException {
        String bitsResidue;
        ArrayList<Byte> bytesListToWrite = new ArrayList<>();
        int counter = 0;
        StringBuilder requiredBitSet = new StringBuilder();
        bitsResidue = null;
        while (counter < bitSequence.length()) {
            for (int i = counter; i < bitSequence.length(); i++) {
                requiredBitSet.append(bitSequence.charAt(i));
                if (decodingTable.containsKey(String.valueOf(requiredBitSet))) {
                    bytesListToWrite.add(decodingTable.get(String.valueOf(requiredBitSet)));
                    counter = i + 1;
                    requiredBitSet.setLength(0);
                    break;
                }
            }
            if (requiredBitSet.length() > 0) {
                bitsResidue = String.valueOf(requiredBitSet);
                counter += requiredBitSet.length();
            }
        }
        pipedOutputStream.write(fromListToArray(bytesListToWrite));
        bitSequence.setLength(0);
        return bitsResidue;
    }

    private byte[] fromListToArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    private String getBinaryStringFromEndByte(byte[] bytesToDecode) {
        String endCode;
        byte endByte = bytesToDecode[bytesToDecode.length - 2];
        if (bytesToDecode[bytesToDecode.length - 1] == 1) {
            String s = Integer.toBinaryString(endByte);
            endCode = s.substring(1, s.length());
        } else {
            endCode = Integer.toBinaryString(endByte);
        }
        return endCode;
    }

    private void writeFile(String fileName, PipedInputStream pipedInputStream) {
        FileOutputStream outputStream;
        try {
            File file = new File(fileName);
            file.createNewFile();
            if (file.exists()) {
                file.delete();
            }
            outputStream = new FileOutputStream(file, true);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = pipedInputStream.read(buffer);
            while (bufferSize != -1) {
                byte[] bytesToWrite = Arrays.copyOfRange(buffer, 0, bufferSize);
                outputStream.write(bytesToWrite);
                bufferSize = pipedInputStream.read(buffer);
            }
            outputStream.close();
            pipedInputStream.close();
            System.out.println(MESSAGE_FILE_CREATED + fileName);
        } catch (IOException e) {
            System.out.println(MESSAGE_FILE_COULD_NOT_BE_RESTORED);
            e.printStackTrace();
        }
    }

    private String createFileName(String archiveName) {
        String fileName = archiveName.substring(0, archiveName.length() - ADDITIONAL_ARCHIVE_EXTENSION.length());
        String[] nameAndExtension = fileName.split("\\.");
        return nameAndExtension[0] + "copy." + nameAndExtension[1];
    }
}