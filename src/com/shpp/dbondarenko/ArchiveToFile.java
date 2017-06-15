package com.shpp.dbondarenko;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * File: ArchiveToFile.java
 * Created by Dmitro Bondarenko on 06.06.2017.
 */
public class ArchiveToFile {
    private static final String FILE_EXTENSION = "-bds";
    private HashMap<String, Byte> codingTable;

    public void restoreFileFromArchive(String fileName) {
        codingTable = new HashMap<>();
        try {
            final PipedOutputStream output = new PipedOutputStream();
            final PipedInputStream input = new PipedInputStream(output);
            Thread ReaderThread = new Thread(new Runnable() {
                private FileInputStream fileInputStream;

                @Override
                public void run() {
                    try {
                        StringBuilder bitSequence = new StringBuilder();
                        fileInputStream = new FileInputStream(fileName);
                        byte[] bytes = new byte[2];
                        int data = fileInputStream.read(bytes);
                        int countByteOfTable = 0;
                        if (data != -1) {
                            countByteOfTable = getCountByteOfTable(bytes[0], bytes[1]);
                        }
                        bytes = new byte[countByteOfTable];
                        data = fileInputStream.read(bytes);
                        if (data != -1) {
                            restoreCodingTable(bytes);
                        }
                        bytes = new byte[1024];
                        data = fileInputStream.read(bytes);
                        String ostatok = null;
                        while (data != -2) {
                            byte[] copyBytes = Arrays.copyOf(bytes, data);
                            if ((data = fileInputStream.read(bytes)) != -1) {
                                if (ostatok != null) {
                                    bitSequence.append(ostatok);
                                    ostatok = null;
                                }
                                ArrayList<Byte> arrayList1 = new ArrayList<>();
                                for (byte b : copyBytes) {
                                    bitSequence.append(toBinaryStringFromByte(b));
                                }
                                int bitSequenceLength = 0;
                                StringBuilder desiredBitSet = new StringBuilder();
                                while (bitSequenceLength < bitSequence.length()) {
                                    for (int i = bitSequenceLength; i < bitSequence.length(); i++) {
                                        desiredBitSet.append(bitSequence.charAt(i));
                                        if (codingTable.containsKey(String.valueOf(desiredBitSet))) {
                                            arrayList1.add(codingTable.get(String.valueOf(desiredBitSet)));
                                            bitSequenceLength = i + 1;
                                            desiredBitSet.setLength(0);
                                            break;
                                        }
                                    }
                                    if (desiredBitSet.length() > 0) {
                                        ostatok = String.valueOf(desiredBitSet);
                                        bitSequenceLength += desiredBitSet.length();
                                    }
                                }
                                output.write(fromListToArray(arrayList1));
                                bitSequence.setLength(0);
                            } else {
                                String endCode;
                                byte endByte = copyBytes[copyBytes.length - 2];
                                if (copyBytes[copyBytes.length - 1] == 1) {
                                    String s = Integer.toBinaryString(endByte);
                                    endCode = s.substring(1, s.length());
                                } else {
                                    endCode = Integer.toBinaryString(endByte);
                                }
                                if (ostatok != null) {
                                    bitSequence.insert(0, ostatok);
                                    ostatok = null;
                                }
                                ArrayList<Byte> arrayList1 = new ArrayList<>();
                                for (int i = 0; i < copyBytes.length - 2; i++) {
                                    byte b = copyBytes[i];
                                    bitSequence.append(toBinaryStringFromByte(b));
                                }
                                bitSequence.append(endCode);
                                int bitSequenceLength = 0;
                                StringBuilder desiredBitSet = new StringBuilder();
                                while (bitSequenceLength < bitSequence.length()) {
                                    for (int i = bitSequenceLength; i < bitSequence.length(); i++) {
                                        desiredBitSet.append(bitSequence.charAt(i));
                                        if (codingTable.containsKey(String.valueOf(desiredBitSet))) {
                                            arrayList1.add(codingTable.get(String.valueOf(desiredBitSet)));
                                            bitSequenceLength = i + 1;
                                            desiredBitSet = new StringBuilder();
                                            break;
                                        }
                                    }
                                    if (desiredBitSet.length() > 0) {
                                        ostatok = String.valueOf(desiredBitSet);
                                    }
                                }
                                output.write(fromListToArray(arrayList1));
                                bitSequence.setLength(0);
                                data = -2;
                            }
                        }
                        System.out.println("archiving finish");
                        fileInputStream.close();
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread WriterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(createFileName(fileName));
                        file.createNewFile();
                        if (file.exists()) {
                            file.delete();
                        }
                        FileOutputStream outputStream;
                        outputStream = new FileOutputStream(file, true);
                        byte bytes[] = new byte[1024];
                        int data = input.read(bytes);
                        while (data != -1) {
                            byte[] a = Arrays.copyOfRange(bytes, 0, data);
                            outputStream.write(a);
                            data = input.read(bytes);
                        }
                        outputStream.close();
                        input.close();
                        System.out.println("file write finish");
                    } catch (IOException e) {
                        System.out.println("Sorry. Such file was not found.");
                        e.printStackTrace();
                    }
                }
            });

            ReaderThread.start();
            WriterThread.start();
        } catch (IOException e) {
            System.out.println("Sorry. Create an archive failed.");
            e.printStackTrace();
        }
    }

    private void restoreCodingTable(byte[] bytesOfTable) {
        for (int j = 0, i = 0; i < bytesOfTable.length; j++, i = j * 3) {
            byte firstByte = bytesOfTable[i];
            StringBuilder secondByte = toBinaryStringFromByte(bytesOfTable[i + 1]);
            StringBuilder thirdByte = toBinaryStringFromByte(bytesOfTable[i + 2]);
            String idByte = removeLeadingZeros(secondByte.append(thirdByte));
            idByte = idByte.substring(1);
            codingTable.put(idByte, firstByte);
        }
    }

    private String removeLeadingZeros(StringBuilder line) {
        int number = Integer.parseInt(String.valueOf(line), 2);
        return Integer.toBinaryString(number);
    }

    private String createFileName(String archiveName) {
        String fileName = archiveName.substring(0, archiveName.length() - FILE_EXTENSION.length());
        String[] nameAndExtension = fileName.split("\\.");
        return nameAndExtension[0] + "copy." + nameAndExtension[1];
    }

    private byte[] fromListToArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    private int getCountByteOfTable(byte firstBit, byte secondBit) {
        StringBuilder firstByte = toBinaryStringFromByte(firstBit);
        StringBuilder secondByte = toBinaryStringFromByte(secondBit);
        return Integer.parseInt(String.valueOf(firstByte.append(secondByte)), 2);
    }

    private StringBuilder toBinaryStringFromByte(byte oneByte) {
        StringBuilder firstByte = new StringBuilder();
        firstByte.append(Integer.toBinaryString(oneByte & 255 | 256).substring(1));
        String s = Integer.toBinaryString(oneByte);
        return firstByte;
    }
}