package com.shpp.dbondarenko;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * File: com.shpp.dbondarenko.FileToArchive.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class FileToArchive {
    private static final String FILE_EXTENSION = "-bds";
    private HashMap<Byte, String> codingTable;

    public void createArchive(String fileName) {
        try {
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
                            output.write(b);
                            data = fileInputStream.read(b);
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
                            for (byte oneByte : b) {
                                if (treeLeavesMap.containsKey(oneByte)) {
                                    treeLeavesMap.get(oneByte).setFrequency(treeLeavesMap.get
                                            (oneByte).getFrequency() + 1);
                                } else {
                                    ArrayList<Byte> bytes = new ArrayList<>();
                                    bytes.add(oneByte);
                                    treeLeavesMap.put(oneByte, new HafmannTreeNode(bytes, 1, null, null));
                                }
                            }

                            data = input.read(b);
                        }
                        input.close();
                        System.out.println("input = finish");


                        ArrayList<HafmannTreeNode> treeLeaves = new ArrayList<>(treeLeavesMap.values());
                        Collections.sort(treeLeaves);
       /* for (HafmannTreeNode leaf : treeLeaves) {
            System.out.println(leaf);
        }*/
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

        System.out.println("main finish");
        try {
            final PipedOutputStream output = new PipedOutputStream();
            final PipedInputStream input = new PipedInputStream(output);

            Thread thread1 = new Thread(new Runnable() {
                private FileInputStream fileInputStream;

                @Override
                public void run() {
                    try {
                        StringBuilder bitSequence = new StringBuilder();
                        fileInputStream = new FileInputStream(fileName);
                        byte bytes[] = new byte[1024];
                        int data = fileInputStream.read(bytes);
                        String ostatok = null;
                        while (data != -1) {
                            for (byte b : bytes) {
                                bitSequence.append(codingTable.get(b));
                            }
                            if (ostatok != null) {
                                bitSequence.insert(0, ostatok);
                                ostatok = null;
                            }
                            int endBits = bitSequence.length() % 8;
                            if (endBits != 0) {
                                ostatok = bitSequence.substring(bitSequence.length() - endBits,
                                        bitSequence.length());
                                bitSequence = new StringBuilder(bitSequence.substring(0, bitSequence.length() - 8));
                            }

                            String[] split = String.valueOf(bitSequence).split("(?<=\\G.{8})");
                            byte[] arrayList = new byte[split.length];
                            for (int i = 0; i < split.length - 1; i++) {
                                String str = split[i];
                                arrayList[i] = (byte) Integer.parseInt(str, 2);
                            }
                            //output.write(String.valueOf(bitSequence).getBytes());
                            output.write(arrayList);
                            bitSequence.setLength(0);
                            //output.write(bytes);
                            data = fileInputStream.read(bytes);
                        }

                        if (ostatok != null) {
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
                        }
                        System.out.println("archiving finish");
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
                        File file = new File(createFileName(fileName));
                        file.createNewFile();
                        FileOutputStream outputStream;
                        outputStream = new FileOutputStream(file, true);

                        byte bytes[] = new byte[1024];
                        int data = input.read(bytes);
                        while (data != -1) {
                            outputStream.write(bytes);
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

    private void writeBytesToFile(byte[] bytesToFile, String fileName) {
       /* System.out.println("bytesToFile: " + bytesToFile.length);
        for (byte b : bytesToFile) {
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

    /* private String createFileName(String fileName) {
         String[] nameAndExtension = fileName.split("\\.");
         return nameAndExtension[0] + FILE_EXTENSION;
     }*/
    private String createFileName(String fileName) {
        return fileName + FILE_EXTENSION;
    }


    private byte[] archiveFile(byte[] bytesFromFile, HashMap<Byte, String> codingTable) {
        StringBuilder bitSequence = createBitsLine(bytesFromFile, codingTable);
        //System.out.println("bitSequence: " + bitSequence);
        // System.out.println("bitSequence length: " + bitSequence.length());
        return getBytes(bitSequence);
    }

    private byte[] getBytes(StringBuilder bitSequence) {
        ArrayList<Byte> bytesList = new ArrayList<>();
        byte firstByte;
        byte secondByte;
        for (Map.Entry entry : codingTable.entrySet()) {
            if (entry.getKey() != null) {
                firstByte = (Byte) entry.getKey();
                bytesList.add(firstByte);
                //System.out.println(firstByte);
                StringBuilder bitsOfValue = new StringBuilder(entry.getValue().toString());
                if (bitsOfValue.length() < 16) {
                    bitsOfValue.insert(0, "1");
                }
                while (bitsOfValue.length() < 16) {
                    bitsOfValue.insert(0, "0");
                }
                firstByte = (byte) Integer.parseInt(bitsOfValue.substring(0, 8), 2);
                bytesList.add(firstByte);
                // System.out.println("bitsOfValue: " + bitsOfValue);
                secondByte = (byte) Integer.parseInt(bitsOfValue.substring(8, 16), 2);
                bytesList.add(secondByte);
                //System.out.println(firstByte);
            } else {
                firstByte = 1;
                bytesList.add(0, firstByte);
                firstByte = (byte) Integer.parseInt(codingTable.get(null), 2);
                bytesList.add(1, firstByte);
            }

        }
        StringBuilder countBiteOfTable = new StringBuilder(Integer.toBinaryString
                ((codingTable.size() - 1) * 3));
        // System.out.println("countBiteOfTable: " + countBiteOfTable);
        while (countBiteOfTable.length() < 16) {
            countBiteOfTable.insert(0, "0");
        }
        firstByte = (byte) Integer.parseInt(countBiteOfTable.substring(0, 8), 2);
        bytesList.add(0, firstByte);
        secondByte = (byte) Integer.parseInt(countBiteOfTable.substring(8, 16), 2);
        bytesList.add(1, secondByte);
        // System.out.println("countBiteOfTable: " + countBiteOfTable);
        // System.out.println("List size: " + bytesList.size());
        for (int j = 0, i = 0; i < bitSequence.length(); j++, i = j * 8) {
            String subString = bitSequence.substring(i, i + 8);
            firstByte = (byte) Integer.parseInt(subString, 2);
            bytesList.add(firstByte);
            // bitSequence = bitSequence.delete(0, 8);
            //System.out.println(firstByte);
        }
        //System.out.println("List size: " + bytesList.size());

        return fromListToArray(bytesList);
    }

    private byte[] fromListToArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    private StringBuilder createBitsLine(byte[] bytesFromFile, HashMap<Byte, String>
            codingTable) {
        StringBuilder bitSequence = new StringBuilder();
        StringBuilder missingNumberOfBits = new StringBuilder();
        for (byte oneByte : bytesFromFile) {
            bitSequence.append(codingTable.get(oneByte));
        }
        int numberOfBitsInLastByte = bitSequence.length() % 8;
        // System.out.println("bitSequence: " + bitSequence);
        // System.out.println("numberOfBitsInLastByte:" + numberOfBitsInLastByte);
        if (numberOfBitsInLastByte != 0) {
            while (numberOfBitsInLastByte != 8) {
                missingNumberOfBits.append("1");
                numberOfBitsInLastByte++;
            }
            bitSequence.append(missingNumberOfBits);
        }
        codingTable.put(null, missingNumberOfBits.toString());
        return bitSequence;
    }

    private HashMap<Byte, String> createCodingTable(byte[] bytesFromFile) {
        ArrayList<HafmannTreeNode> treeLeaves = createHafmannTreeLeaves(bytesFromFile);
       /* for (HafmannTreeNode leaf : treeLeaves) {
            System.out.println(leaf);
        }*/
        // System.out.println("treeLeaves: " + treeLeaves.size());
        HafmannTreeNode hafmannTreeRoot = buildHuffmanTree(treeLeaves);
        // System.out.println("treeLeaves: " + treeLeaves.size());
        //  System.out.println("hafmannTreeRoot: " + hafmannTreeRoot);
        HashMap<Byte, String> hafmannTable = createHafmannTable(treeLeaves,
                hafmannTreeRoot);
       /* for (Map.Entry entry : hafmannTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
        return hafmannTable;
    }


    private HashMap<Byte, String> createHafmannTable(ArrayList<HafmannTreeNode> treeLeaves,
                                                     HafmannTreeNode hafmannTreeRoot) {
        HashMap<Byte, String> hafmannTable = new HashMap<>();
        //System.out.println("treeLeaves: " + treeLeaves.size());
        /*byte oneByte = treeLeaves.get(0).getBytes().get(treeLeaves.get(0).getBytes
().size()-1);
        System.out.println(oneByte);
        String idByte = createIdByte(oneByte, hafmannTreeRoot);
        System.out.println(idByte);*/
        for (HafmannTreeNode leaf : treeLeaves) {
            byte oneByte = leaf.getBytes().get(0);
            String idByte = createIdByte(oneByte, hafmannTreeRoot);
            hafmannTable.put(oneByte, idByte);
        }
        // System.out.println("hafmannTable: " + hafmannTable.size());
        // System.out.println("treeLeaves: " + treeLeaves.size());
        return hafmannTable;
    }

    private String createIdByte(Byte oneByte, HafmannTreeNode hafmannTreeRoot) {
        String idByte = "";
        if (hafmannTreeRoot.getLeftChild() != null &&
                hafmannTreeRoot.getLeftChild().getBytes().contains(oneByte)) {
            idByte = "0" + createIdByte(oneByte, hafmannTreeRoot.getLeftChild());
        }
        if (hafmannTreeRoot.getRightChild() != null &&
                hafmannTreeRoot.getRightChild().getBytes().contains(oneByte)) {
            idByte = "1" + createIdByte(oneByte, hafmannTreeRoot.getRightChild());
        }
        return idByte;
    }

    private HafmannTreeNode buildHuffmanTree(ArrayList<HafmannTreeNode> treeLeaves) {
        ArrayList<HafmannTreeNode> treeNodes = new ArrayList<>(treeLeaves);
        // System.out.println("size " + treeNodes.size());
        while (treeNodes.size() > 1) {
            ArrayList<Byte> bytes = new ArrayList<>();
            HafmannTreeNode leftChild;
            HafmannTreeNode rightChild;
            if (treeNodes.get(0).getFrequency() >= treeNodes.get(1).getFrequency()) {
                rightChild = treeNodes.get(0);
                leftChild = treeNodes.get(1);
            } else {
                rightChild = treeNodes.get(1);
                leftChild = treeNodes.get(0);
            }
            bytes.addAll(treeNodes.get(0).getBytes());
            bytes.addAll(treeNodes.get(1).getBytes());
            int frequency = treeNodes.get(0).getFrequency() + treeNodes.get
                    (1).getFrequency();
            treeNodes.add(new HafmannTreeNode(bytes, frequency, leftChild, rightChild));
           /* System.out.println(new HafmannTreeNode(bytes, frequency, treeNodes.get(0),
            treeNodes.get(1)));*/
            // System.out.println("size before: " + treeNodes.size());
            treeNodes.remove(0);
            treeNodes.remove(0);
            Collections.sort(treeNodes);
            // System.out.println("size after: " + treeNodes.size());
        }
        return treeNodes.get(0);
    }

    private ArrayList<HafmannTreeNode> createHafmannTreeLeaves(byte[] bytesFromFile) {
        HashMap<Byte, HafmannTreeNode> treeLeavesMap = new HashMap<>();
        for (byte oneByte : bytesFromFile) {
            if (treeLeavesMap.containsKey(oneByte)) {
                treeLeavesMap.get(oneByte).setFrequency(treeLeavesMap.get
                        (oneByte).getFrequency() + 1);
            } else {
                ArrayList<Byte> bytes = new ArrayList<>();
                bytes.add(oneByte);
                treeLeavesMap.put(oneByte, new HafmannTreeNode(bytes, 1, null, null));
            }
        }
        ArrayList<HafmannTreeNode> treeLeaves = new ArrayList<>(treeLeavesMap.values());
        Collections.sort(treeLeaves);
        return treeLeaves;
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
