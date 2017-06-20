package com.shpp.dbondarenko.archiver.bds;

import com.shpp.dbondarenko.util.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * File: ArchiveToFile.java
 * Class in which the file from the archive is restored.
 * Created by Dmitro Bondarenko on 06.06.2017.
 */
public class ArchiveToFile extends Utility {
    // The table by which bytes are restored.
    private HashMap<String, Byte> decodingTable;

    /**
     * Restore file from archive.
     *
     * @param archiveName The name of the archive from which to restore the file.
     */
    public void restoreFileFromArchive(String archiveName) {
        if (isFileExist(archiveName)) {
            try {
                final PipedOutputStream pipedOutputStream = new PipedOutputStream();
                final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                Thread ReaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileInputStream fileInputStream;
                        try {
                            System.out.println(MESSAGE_PLEASE_WAIT);
                            fileInputStream = new FileInputStream(archiveName);
                            int countByteOfTable = getCountByteOfTable(fileInputStream);
                            restoreDecodingTable(fileInputStream, countByteOfTable);
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
                        writeFile(createFileName(archiveName), pipedInputStream,
                                MESSAGE_FILE_CREATED, MESSAGE_FILE_COULD_NOT_BE_RESTORED);
                    }
                });
                ReaderThread.start();
                WriterThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(MESSAGE_FILE_NOT_FOUND);
        }
    }

    /**
     * Get the count of bytes of the table to decode.
     *
     * @param fileInputStream The stream for reading bytes from a file.
     * @return The number of bytes you need to read to restore the table for decoding.
     */
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

    /**
     * Restore the table for decoding.
     *
     * @param fileInputStream  The stream for reading bytes from a file.
     * @param countByteOfTable The number of bytes you need to read to restore the table for decoding.
     */
    private void restoreDecodingTable(FileInputStream fileInputStream, int countByteOfTable) throws IOException {
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

    /**
     * Translate the byte into a bit string representation.
     * During the transfer in the high-order bits, the zeros do not disappear.
     *
     * @param oneByte The byte.
     * @return The string of bits.
     */
    private StringBuilder toBinaryStringFromByte(byte oneByte) {
        StringBuilder firstByte = new StringBuilder();
        firstByte.append(Integer.toBinaryString(oneByte & 255 | 256).substring(1));
        return firstByte;
    }

    /**
     * Remove the leading zeros.
     *
     * @param line The string consisting of bits.
     * @return The string of bits.
     */
    private String removeLeadingZeros(StringBuilder line) {
        int number = Integer.parseInt(String.valueOf(line), BINARY_SYSTEM);
        return Integer.toBinaryString(number);
    }

    /**
     * Decode the archive. From the file, the bits are read into the buffer.
     * They are translated using the decoding table into a string sequence of bits.
     * From this sequence, the bits of the original file are restored.
     * These bits are transferred to the PipedInputStream pipeline stream for writing to the file.
     *
     * @param fileInputStream   The stream for reading bytes from a file.
     * @param pipedOutputStream The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     */
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
                String bitsOfLastByte = getBinaryStringFromEndByte(bytesToDecode);
                createBitSequence(bitSequence, bitsResidue, bytesToDecode, bytesToDecode.length - 2);
                bitSequence.append(bitsOfLastByte);
                bitsResidue = restoreByte(pipedOutputStream, bitSequence);
                bufferSize = -2;
            }
        }
    }

    /**
     * Create a bit sequence with bytes using a decoding table.
     *
     * @param bitSequence     The string for storing the bit sequence.
     * @param bitsResidue     The bits residue from the previous bit sequence.
     * @param bytesToDecode   The array of bytes to convert to a bit sequence.
     * @param iterationsCount The count of iterations for converting bytes to bit sequence.
     */
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

    /**
     * Restore the byte using the decoding table.
     *
     * @param pipedOutputStream The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     * @param bitSequence       The string for storing the bit sequence.
     * @return The bits residue from the bit sequence.
     */
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

    /**
     * Create an array of bits from the list of bits.
     *
     * @param bytesList The list of bits.
     * @return The array of bits.
     */
    private byte[] fromListToArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    /**
     * Get the bit sequence from the last byte.
     *
     * @param bytesToDecode An array of bytes to decode.
     * @return The bit sequence from the last byte.
     */
    private String getBinaryStringFromEndByte(byte[] bytesToDecode) {
        String bitsOfLastByte;
        byte endByte = bytesToDecode[bytesToDecode.length - 2];
        if (bytesToDecode[bytesToDecode.length - 1] == 1) {
            String s = Integer.toBinaryString(endByte);
            bitsOfLastByte = s.substring(1, s.length());
        } else {
            bitsOfLastByte = Integer.toBinaryString(endByte);
        }
        return bitsOfLastByte;
    }

    /**
     * Create a new file name.
     *
     * @param archiveName The name of the archive from which the file is restored.
     * @return The new file name.
     */
    private String createFileName(String archiveName) {
        String fileName = archiveName.substring(0, archiveName.length() - ADDITIONAL_ARCHIVE_EXTENSION.length());
        String[] nameAndExtension = fileName.split("\\.");
        return nameAndExtension[0] + ENDING_NAME_OF_NEW_FILE + nameAndExtension[1];
    }
}