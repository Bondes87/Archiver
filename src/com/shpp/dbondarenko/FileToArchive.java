package com.shpp.dbondarenko;

import java.io.*;
import java.util.*;

/**
 * File: com.shpp.dbondarenko.FileToArchive.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class FileToArchive {
    private static final String FILE_EXTENSION = "-bds";
    private static final int BUFFER_SIZE_FOR_READING_AND_WRITING = 1024;
    private HashMap<Byte, String> codingTable;

    public void createArchiveFromFile(String fileName) {
        try {
            System.out.println("Please wait!!!");
            createCodingTable(fileName);
            encodeFile(fileName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCodingTable(String fileName) throws IOException,
            InterruptedException {
        final PipedOutputStream output = new PipedOutputStream();
        final PipedInputStream input = new PipedInputStream(output);
        Thread readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readFile(fileName, output);
            }
        });
        Thread createCodingTableThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<HuffmanTreeNode> treeLeaves = createLeavesOfHuffmanTree
                            (input);
                    HuffmanTreeNode huffmanTreeRoot = buildHuffmanTree(treeLeaves);
                    codingTable = createHuffmanTable(treeLeaves, huffmanTreeRoot);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.start();
        createCodingTableThread.start();
        createCodingTableThread.join();
    }

    private void encodeFile(String fileName) throws IOException {
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        Thread codingFileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createBytesForWriting(pipedOutputStream, fileName);
            }
        });
        Thread WriterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeFile(fileName, pipedInputStream);
            }

        });
        codingFileThread.start();
        WriterThread.start();
    }

    private void readFile(String fileName, PipedOutputStream output) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = fileInputStream.read(buffer);
            while (bufferSize != -1) {
                byte[] bytesToRead = Arrays.copyOfRange(buffer, 0, bufferSize);
                output.write(bytesToRead);
                bufferSize = fileInputStream.read(buffer);
            }
            fileInputStream.close();
            output.close();
        } catch (IOException e) {
            System.out.println("Sorry. Such file was not found.");
            e.printStackTrace();
        }
    }

    private ArrayList<HuffmanTreeNode> createLeavesOfHuffmanTree(PipedInputStream input)
            throws IOException {
        HashMap<Byte, HuffmanTreeNode> treeLeavesMap = new HashMap<>();
        byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
        int bufferSize = input.read(buffer);
        while (bufferSize != -1) {
            for (int i = 0; i < bufferSize; i++) {
                byte oneByte = buffer[i];
                if (treeLeavesMap.containsKey(oneByte)) {
                    treeLeavesMap.get(oneByte).setFrequency(treeLeavesMap.get
                            (oneByte).getFrequency() + 1);
                } else {
                    ArrayList<Byte> bytes = new ArrayList<>();
                    bytes.add(oneByte);
                    treeLeavesMap.put(oneByte, new HuffmanTreeNode(bytes, 1, null, null));
                }
            }
            bufferSize = input.read(buffer);
        }
        input.close();
        ArrayList<HuffmanTreeNode> treeLeaves = new ArrayList<>(treeLeavesMap.values());
        Collections.sort(treeLeaves);
        return treeLeaves;
    }

    private HuffmanTreeNode buildHuffmanTree(ArrayList<HuffmanTreeNode> treeLeaves) {
        ArrayList<HuffmanTreeNode> treeNodes = new ArrayList<>(treeLeaves);
        while (treeNodes.size() > 1) {
            ArrayList<Byte> bytes = new ArrayList<>();
            HuffmanTreeNode leftChild;
            HuffmanTreeNode rightChild;
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
            treeNodes.add(new HuffmanTreeNode(bytes, frequency, leftChild, rightChild));
            treeNodes.remove(0);
            treeNodes.remove(0);
            Collections.sort(treeNodes);
        }
        return treeNodes.get(0);
    }

    private HashMap<Byte, String> createHuffmanTable(ArrayList<HuffmanTreeNode> treeLeaves,
                                                     HuffmanTreeNode huffmanTreeRoot) {
        HashMap<Byte, String> huffmanTable = new HashMap<>();
        for (HuffmanTreeNode leaf : treeLeaves) {
            byte oneByte = leaf.getBytes().get(0);
            String idByte = createIdByte(oneByte, huffmanTreeRoot);
            huffmanTable.put(oneByte, idByte);
        }
        return huffmanTable;
    }

    private String createIdByte(Byte oneByte, HuffmanTreeNode hafmannTreeRoot) {
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

    private void createBytesForWriting(PipedOutputStream pipedOutputStream, String
            fileName) {
        try {
            FileInputStream fileInputStream;
            pipedOutputStream.write(getBytesForCodingTable());
            StringBuilder bitSequence = new StringBuilder();
            fileInputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = fileInputStream.read(buffer);
            String bitsResidue = null;
            while (bufferSize != -1) {
                if (bitsResidue != null) {
                    bitSequence.insert(0, bitsResidue);
                    bitsResidue = null;
                }
                for (int i = 0; i < bufferSize; i++) {
                    byte b = buffer[i];
                    bitSequence.append(codingTable.get(b));
                }
                int countOfBitsInLastByte = bitSequence.length() % 8;
                if (countOfBitsInLastByte != 0) {
                    bitsResidue = bitSequence.substring(bitSequence.length() -
                            countOfBitsInLastByte, bitSequence.length());
                    bitSequence = new StringBuilder(bitSequence.substring(0,
                            bitSequence.length() - countOfBitsInLastByte));
                }
                writeBitSequence(pipedOutputStream, bitSequence);
                bufferSize = fileInputStream.read(buffer);
            }
            writeBitsResidue(pipedOutputStream, bitsResidue);
            fileInputStream.close();
            pipedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBitSequence(PipedOutputStream pipedOutputStream, StringBuilder
            bitSequence) throws IOException {
        if (bitSequence.length() >= 8) {
            String[] split = String.valueOf(bitSequence).split("(?<=\\G.{8})");
            byte[] arrayList = new byte[split.length];
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                arrayList[i] = (byte) Integer.parseInt(str, 2);
            }
            pipedOutputStream.write(arrayList);
            bitSequence.setLength(0);
        }
    }

    private void writeBitsResidue(PipedOutputStream pipedOutputStream, String bitsResidue)
            throws IOException {
        if (bitsResidue != null) {
            byte[] array = new byte[2];
            if (bitsResidue.charAt(0) == '0') {
                bitsResidue = "1" + bitsResidue;
                array[0] = (byte) Integer.parseInt(bitsResidue, 2);
                array[1] = (byte) Integer.parseInt("00000001", 2);
            } else {
                array[0] = (byte) Integer.parseInt(bitsResidue, 2);
                array[1] = (byte) Integer.parseInt("00000000", 2);
            }
            pipedOutputStream.write(array);
        }
    }

    private byte[] getBytesForCodingTable() {
        byte[] bytesFromCodingTable = new byte[codingTable.size() * 3 + 2];
        byte firstByte;
        byte secondByte;
        int index = 0;
        StringBuilder countBiteOfTable = new StringBuilder(Integer.toBinaryString
                (codingTable.size() * 3));
        while (countBiteOfTable.length() < 16) {
            countBiteOfTable.insert(0, "0");
        }
        firstByte = (byte) Integer.parseInt(countBiteOfTable.substring(0, 8), 2);
        bytesFromCodingTable[index++] = firstByte;
        secondByte = (byte) Integer.parseInt(countBiteOfTable.substring(8, 16), 2);
        bytesFromCodingTable[index++] = secondByte;
        for (Map.Entry entry : codingTable.entrySet()) {
            firstByte = (Byte) entry.getKey();
            bytesFromCodingTable[index++] = firstByte;
            StringBuilder bitsOfValue = new StringBuilder(entry.getValue().toString());
            if (bitsOfValue.length() < 16) {
                bitsOfValue.insert(0, "1");
            }
            while (bitsOfValue.length() < 16) {
                bitsOfValue.insert(0, "0");
            }
            firstByte = (byte) Integer.parseInt(bitsOfValue.substring(0, 8), 2);
            bytesFromCodingTable[index++] = firstByte;
            secondByte = (byte) Integer.parseInt(bitsOfValue.substring(8, 16), 2);
            bytesFromCodingTable[index++] = secondByte;
        }
        return bytesFromCodingTable;
    }

    private void writeFile(String fileName, PipedInputStream pipedInputStream) {
        FileOutputStream fileOutputStream;
        try {
            String archiveName = fileName + FILE_EXTENSION;
            File archive = new File(archiveName);
            if (archive.exists()) {
                archive.delete();
            }
            archive.createNewFile();
            fileOutputStream = new FileOutputStream(archive, true);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = pipedInputStream.read(buffer);
            while (bufferSize != -1) {
                byte[] bytesToWrite = Arrays.copyOfRange(buffer, 0, bufferSize);
                fileOutputStream.write(bytesToWrite);
                bufferSize = pipedInputStream.read(buffer);
            }
            fileOutputStream.close();
            pipedInputStream.close();
            System.out.println("Archive " + archiveName + " created");
        } catch (IOException e) {
            System.out.println("Sorry. Create an archive failed.");
            e.printStackTrace();
        }
    }
}