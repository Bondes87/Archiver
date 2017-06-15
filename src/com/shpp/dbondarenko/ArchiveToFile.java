package com.shpp.dbondarenko;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * File: ArchiveToFile.java
 * Created by Dmitro Bondarenko on 06.06.2017.
 */
public class ArchiveToFile {
    private static final String FILE_EXTENSION = "-bds";
    private HashMap<String, Byte> codingTable;

    public void unarchive(String fileName) {
        codingTable = new HashMap<>();
        readFile("art.txt");
        for (Map.Entry entry : codingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

       /* try {
            final PipedOutputStream output = new PipedOutputStream();
            final PipedInputStream input = new PipedInputStream(output);

            Thread thread1 = new Thread(new Runnable() {
                private FileInputStream fileInputStream;

                @Override
                public void run() {
                    try {
                        fileInputStream = new FileInputStream(fileName);
                        byte b[] = new byte[1024];
                        int data = fileInputStream.read(b);
                        while (data != -1) {
                            byte[] a = Arrays.copyOfRange(b, 0, data);
                            output.write(a);
                            data = fileInputStream.read(b);
                            // System.out.println(Arrays.toString(a));
                        }
                        System.out.println("output = finish");
                        fileInputStream.close();
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HashMap<Byte, HafmannTreeNode> treeLeavesMap = new HashMap<>();
                        byte b[] = new byte[1024];
                        int data = input.read(b);
                        while (data != -1) {
                            for (int i = 0; i < data; i++) {
                                byte oneByte = b[i];
                                if (treeLeavesMap.containsKey(oneByte)) {
                                    treeLeavesMap.get(oneByte).setFrequency
                                            (treeLeavesMap.get
                                                    (oneByte).getFrequency() + 1);
                                } else {
                                    ArrayList<Byte> bytes = new ArrayList<>();
                                    bytes.add(oneByte);
                                    treeLeavesMap.put(oneByte, new HafmannTreeNode(bytes,
                                            1, null, null));
                                }
                            }

                            data = input.read(b);
                        }
                        input.close();
                        System.out.println("input = finish");


                        ArrayList<HafmannTreeNode> treeLeaves = new ArrayList<>
                                (treeLeavesMap.values());
                        Collections.sort(treeLeaves);
       *//* for (HafmannTreeNode leaf : treeLeaves) {
            System.out.println(leaf);
        }*//*
                        // System.out.println("treeLeaves: " + treeLeaves.size());
                        HafmannTreeNode hafmannTreeRoot = buildHuffmanTree(treeLeaves);
                        // System.out.println("treeLeaves: " + treeLeaves.size());
                        //  System.out.println("hafmannTreeRoot: " + hafmannTreeRoot);
                        codingTable = createHafmannTable(treeLeaves,
                                hafmannTreeRoot);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread1.start();
            thread2.start();
            System.out.println("main wait");
            thread2.join();
            System.out.println("main restore");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        for (Map.Entry entry : codingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("main finish");*/

        try {
            final PipedOutputStream output = new PipedOutputStream();
            final PipedInputStream input = new PipedInputStream(output);
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(createFileName(fileName));
                        file.createNewFile();
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
                        e.printStackTrace();
                    }

                }
            });
            Thread thread2 = new Thread(new Runnable() {
                private FileInputStream fileInputStream;

                @Override
                public void run() {
                    try {
                        StringBuilder bitSequence = new StringBuilder();
                        fileInputStream = new FileInputStream(fileName);
                        byte bytes[] = new byte[1024];
                        int data = fileInputStream.read(bytes);
                        // System.out.println(data);
                        String ostatok = null;
                        while (data != -2) {
                            System.out.println(Arrays.toString(bytes));
                            byte[] copyBytes = Arrays.copyOf(bytes, data);
                            if ((data = fileInputStream.read(bytes)) != -1) {
                                if (ostatok != null) {
                                    bitSequence.insert(0, ostatok);
                                    ostatok = null;
                                }
                                ArrayList<Byte> arrayList1 = new ArrayList<>();
                                // System.out.println(Arrays.toString(bytes));
                                for (int i = 0; i < data; i++) {
                                    byte b = copyBytes[i];
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
                                            desiredBitSet = new StringBuilder();
                                            //bitSequence.delete(0, i + 1);
                                            break;
                                        }
                                    }
                                    if (desiredBitSet.length() > 0) {
                                        ostatok = String.valueOf(desiredBitSet);
                                    }
                                }
                                output.write(fromListToArray(arrayList1));
                                bitSequence.setLength(0);
                                //System.out.println(bitSequence);
                                // System.out.println(bitSequence.length());
                               /* int endBits = bitSequence.length() % 8;
                                if (endBits != 0) {
                                    ostatok = bitSequence.substring(bitSequence.length() -
                                                    endBits,
                                            bitSequence.length());
                                    bitSequence = new StringBuilder(bitSequence.substring(0,
                                            bitSequence.length() - endBits));
                                }
                                byte[] arrayMy = new byte[bitSequence.length() / 8];
                                System.out.println(arrayMy.length);
                                for (int j = 0, i = 0; i < bitSequence.length(); j++, i = j *
                                        8) {
                                    String subString = bitSequence.substring(i, i + 8);
                                    arrayMy[j] = (byte) Integer.parseInt(subString, 2);
                                }
                                String[] split = String.valueOf(bitSequence).split("(?<=\\G.{8})");
                                // System.out.println(Arrays.toString(split));
                                byte[] arrayList = new byte[split.length];
                                for (int i = 0; i < split.length; i++) {
                                    String str = split[i];
                                    arrayList[i] = (byte) Integer.parseInt(str, 2);
                                }
                                // System.out.println(Arrays.toString(arrayList));
                                //output.write(String.valueOf(bitSequence).getBytes());
                                output.write(arrayList);
                                bitSequence.setLength(0);
                                //output.write(bytes);
                                data = fileInputStream.read(bytes);*/
                            } else {
                                String endCode = "";
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
                                // System.out.println(Arrays.toString(bytes));
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
                                            //bitSequence.delete(0, i + 1);
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

                    /*    if (ostatok != null) {
                            byte[] array = new byte[2];
                            if (ostatok.charAt(0) == '0') {
                                ostatok = "1" + ostatok;
                                array[0] = (byte) Integer.parseInt(ostatok, 2);
                                array[1] = (byte) Integer.parseInt("00000001", 2);
                            } else {
                                array[0] = (byte) Integer.parseInt(ostatok, 2);
                                array[1] = (byte) Integer.parseInt("00000000", 2);
                            }
                            output.write(array);
                        }*/
                        System.out.println("archiving finish");
                        fileInputStream.close();
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread1.start();
            thread2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //byte[] bytesToFile = archiveFile(bytesFromFile, codingTable);
        //writeBytesToFile(bytesToFile, fileName);
       /* for (Map.Entry entry : codingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
        // byte[] bytesToFile = archiveFile(bytesFromFile, codingTable);
        //writeBytesToFile(bytesToFile, fileName);
       /* for (Map.Entry entry : codingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
    }

    private void readFile(String name) {
        try {
            //Объект для чтения файла в буфер
            BufferedReader in = new BufferedReader(new FileReader(name));
            try {
                //В цикле построчно считываем файл
                String s;
                while ((s = in.readLine()) != null) {
                    String[] line = s.split(":");
                    byte b = Byte.parseByte(line[0]);
                    String id = line[1].trim();
                    codingTable.put(id, b);
                }
            } finally {
                //Также не забываем закрыть файл
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /*public void unarchive(String fileName) {
        byte[] bytesFromFile = readFileToBytes(fileName);
       *//* System.out.println("bytesFromFile " + bytesFromFile.length);
        for (byte oneByte : bytesFromFile) {
            System.out.println(oneByte);
        }*//*
        int countByteOfTable = getCountByteOfTable(bytesFromFile);
        // System.out.println("countByteOfTable " + countByteOfTable);
        byte[] bytesOfTable = new byte[countByteOfTable];
        System.arraycopy(bytesFromFile, 4, bytesOfTable, 0, countByteOfTable);
        //System.out.println("bytesOfTable.length " + bytesOfTable.length);
        //System.out.println(bytesOfTable[0]);
        // System.out.println(bytesOfTable[188]);
        HashMap<String, Byte> encodingTable = restoreCodingTable(bytesOfTable);
        StringBuilder extraBits = toBinaryStringFromByte(bytesFromFile[3]);
        System.out.println(extraBits);
        byte[] bytesFromEncodedFile = new byte[bytesFromFile.length - bytesOfTable.length - 4];
        // System.out.println("bytesFromEncodedFile " + bytesFromEncodedFile.length);
        System.arraycopy(bytesFromFile, 4 + countByteOfTable,
                bytesFromEncodedFile, 0, bytesFromEncodedFile.length);
        //System.out.println(Arrays.toString(bytesFromEncodedFile));
        byte[] bytesToFile = unarchiveFile(bytesFromEncodedFile, encodingTable, extraBits);
        //System.out.println("bytesToFile " + bytesToFile.length);
        writeBytesToFile(bytesToFile, fileName);
    }*/

    private void writeBytesToFile(byte[] bytesToFile, String fileName) {
        // System.out.println("bytesToFile: " + bytesToFile.length);
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
       /* System.out.println("bitSequence " + bitSequence);
        for (Map.Entry entry : encodingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
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
        // System.out.println("bytesFromEncodedFile.lenght: " + bytesFromEncodedFile.length);
        StringBuilder bitSequence = new StringBuilder();
        for (byte oneByte : bytesFromEncodedFile) {
            StringBuilder bitsFromByte = toBinaryStringFromByte(oneByte);
            // System.out.println("bitsFromByte " + bitsFromByte);
            bitSequence.append(bitsFromByte);
        }
        // System.out.println(bitSequence);
        // System.out.println(bitSequence.length());
        bitSequence.delete(bitSequence.length() - removeLeadingZeros(extraBits).length(),
                bitSequence.length());
        // System.out.println(bitSequence);
        // System.out.println(bitSequence.length());
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
        // System.out.println(firstByte);
       /* String firstByte = Integer.toBinaryString(bytesFromFile[0]);
        System.out.println(firstByte);*/
        StringBuilder secondByte = toBinaryStringFromByte(bytesFromFile[1]);
        //  System.out.println(secondByte);
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
