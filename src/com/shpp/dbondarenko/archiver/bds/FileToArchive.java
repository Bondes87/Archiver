package com.shpp.dbondarenko.archiver.bds;

import com.shpp.dbondarenko.model.HuffmanTreeNode;
import com.shpp.dbondarenko.util.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * File: FileToArchive.java
 * Class in which an archive is created from the file.
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class FileToArchive extends Utility {
    // The table by which the bytes are coded.
    private HashMap<Byte, String> codingTable;

    /**
     * Create an archive from a file.
     *
     * @param fileName The name of the file from which the archive is created.
     */
    public void createArchiveFromFile(String fileName) {
        if (isFileExist(fileName)) {
            try {
                System.out.println(MESSAGE_PLEASE_WAIT);
                createCodingTable(fileName);
                encodeFile(fileName);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(MESSAGE_FILE_NOT_FOUND);
        }
    }

    /**
     * Create a Huffman table. Two pipeline streams are created here: one reads the file,
     * the other creates a Huffman table based on the data received from the first thread.
     *
     * @param fileName The name of the file from which the archive is created.
     */

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
                    ArrayList<HuffmanTreeNode> treeLeaves = createLeavesOfHuffmanTree(input);
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

    /**
     * Encoding bytes from a file. Two pipeline streams are created here:
     * one reads the file and encodes the bytes using the Huffman table,
     * the other writes down the data received from the first stream to the file.
     *
     * @param fileName The name of the file from which the archive is created.
     */
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
                writeFile(createNewFileName(fileName), pipedInputStream,
                        MESSAGE_ARCHIVE_CREATED, MESSAGE_CREATE_AN_ARCHIVE_FAILED);
            }
        });
        codingFileThread.start();
        WriterThread.start();
    }

    /**
     * Create leaves of the Huffman tree. Returns a list of created leaves based on the data received
     * from the conveyor stream PipedOutputStream.
     *
     * @param input The pipeline stream that receives the bytes from the PipedOutputStream pipeline stream.
     * @return List of Huffman tree leaves.
     */
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

    /**
     * Build a Huffman tree from the resulting leaves. Return the root of the Huffman tree.
     *
     * @param treeLeaves The list of Huffman tree leaves.
     * @return The root of the Huffman tree.
     */
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

    /**
     * Create a Huffman table. The keys of the table are the bytes received from the leaves,
     * and the values are the binary code obtained with the help of the Huffman tree.
     *
     * @param treeLeaves      The list of Huffman tree leaves.
     * @param huffmanTreeRoot The Huffman tree root.
     * @return The Huffman table.
     */
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

    /**
     * Create a binary code for bits using the Huffman tree.
     *
     * @param oneByte         The byte received from Huffman's leaf.
     * @param huffmanTreeRoot The Huffman tree root.
     * @return The string with a bit code.
     */
    private String createIdByte(Byte oneByte, HuffmanTreeNode huffmanTreeRoot) {
        String idByte = "";
        if (huffmanTreeRoot.getLeftChild() != null &&
                huffmanTreeRoot.getLeftChild().getBytes().contains(oneByte)) {
            idByte = "0" + createIdByte(oneByte, huffmanTreeRoot.getLeftChild());
        }
        if (huffmanTreeRoot.getRightChild() != null &&
                huffmanTreeRoot.getRightChild().getBytes().contains(oneByte)) {
            idByte = "1" + createIdByte(oneByte, huffmanTreeRoot.getRightChild());
        }
        return idByte;
    }


    /**
     * Create bytes for writing and send to write.
     *
     * @param pipedOutputStream The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     * @param fileName          The name of the file from which the archive is created.
     */
    private void createBytesForWriting(PipedOutputStream pipedOutputStream, String
            fileName) {
        try {
            FileInputStream fileInputStream;
            // Send coding table bytes for writing to file.
            pipedOutputStream.write(getBytesForCodingTable());
            StringBuilder bitSequence = new StringBuilder();
            fileInputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[BUFFER_SIZE_FOR_READING_AND_WRITING];
            int bufferSize = fileInputStream.read(buffer);
            String bitsResidue = null;
            while (bufferSize != -1) {
                // Create a bit sequence with bytes using a coding table.
                createBitSequence(bitSequence, buffer, bufferSize, bitsResidue);
                bitsResidue = null;
                int countOfBitsInLastByte = bitSequence.length() % COUNT_OF_BITS_IN_BYTE;
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

    /**
     * Create a bit sequence with bytes using a coding table.
     *
     * @param bitSequence     The string for storing the bit sequence.
     * @param bytes           The array of bytes to convert to a bit sequence.
     * @param iterationsCount The count of iterations for converting bytes to bit sequence.
     * @param bitsResidue     The bits residue from the previous bit sequence.
     */
    private void createBitSequence(StringBuilder bitSequence, byte[] bytes,
                                   int iterationsCount, String bitsResidue) {
        if (bitsResidue != null) {
            bitSequence.insert(0, bitsResidue);
        }
        for (int i = 0; i < iterationsCount; i++) {
            byte oneByte = bytes[i];
            bitSequence.append(codingTable.get(oneByte));
        }
    }

    /**
     * Write a bit sequence. The byte array is created from the bit sequence.
     * Bytes are passed to the conveyor stream of PipedInputStream for writing to the file.
     *
     * @param pipedOutputStream The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     * @param bitSequence       The bit sequence with which to create an array of bytes.
     */
    private void writeBitSequence(PipedOutputStream pipedOutputStream, StringBuilder
            bitSequence) throws IOException {
        if (bitSequence.length() >= COUNT_OF_BITS_IN_BYTE) {
            String[] split = String.valueOf(bitSequence).split("(?<=\\G.{8})");
            byte[] arrayList = new byte[split.length];
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                arrayList[i] = (byte) Integer.parseInt(str, BINARY_SYSTEM);
            }
            pipedOutputStream.write(arrayList);
            bitSequence.setLength(0);
        }
    }

    /**
     * Write a bit residue. The byte array is created from the bit residue.
     * Bytes are passed to the conveyor stream of PipedInputStream for writing to the file.
     *
     * @param pipedOutputStream The pipeline stream that transfers bytes to a pipeline stream PipedInputStream.
     * @param bitsResidue       The bit residue with which to create an array of bytes.
     */
    private void writeBitsResidue(PipedOutputStream pipedOutputStream, String bitsResidue)
            throws IOException {
        if (bitsResidue != null) {
            byte[] array = new byte[2];
            if (bitsResidue.charAt(0) == '0') {
                bitsResidue = "1" + bitsResidue;
                array[0] = (byte) Integer.parseInt(bitsResidue, BINARY_SYSTEM);
                array[1] = (byte) Integer.parseInt("00000001", BINARY_SYSTEM);
            } else {
                array[0] = (byte) Integer.parseInt(bitsResidue, BINARY_SYSTEM);
                array[1] = (byte) Integer.parseInt("00000000", BINARY_SYSTEM);
            }
            pipedOutputStream.write(array);
        }
    }

    /**
     * Get the bytes from the coding table. First, the length of the encoding table is written to the array.
     * This length is translated into two bytes. The table key is a byte that is written to an array.
     * Value - this is a binary bit coding, it is translated into two bytes.
     * If the value is less than 16 bits, then one is first added to the 1, and then zeroes.
     *
     * @return The array of bytes from the coding table.
     */
    private byte[] getBytesForCodingTable() {
        byte[] bytesFromCodingTable = new byte[codingTable.size() * 3 + 2];
        byte firstByte;
        byte secondByte;
        int index = 0;
        StringBuilder countBiteOfTable = new StringBuilder(Integer.toBinaryString
                (codingTable.size() * 3));
        while (countBiteOfTable.length() < COUNT_OF_BITS_IN_BYTE * 2) {
            countBiteOfTable.insert(0, "0");
        }
        firstByte = (byte) Integer.parseInt(countBiteOfTable.substring(0, COUNT_OF_BITS_IN_BYTE), BINARY_SYSTEM);
        bytesFromCodingTable[index++] = firstByte;
        secondByte = (byte) Integer.parseInt(countBiteOfTable.substring(COUNT_OF_BITS_IN_BYTE, COUNT_OF_BITS_IN_BYTE * 2), BINARY_SYSTEM);
        bytesFromCodingTable[index++] = secondByte;
        for (Map.Entry entry : codingTable.entrySet()) {
            firstByte = (Byte) entry.getKey();
            bytesFromCodingTable[index++] = firstByte;
            StringBuilder bitsOfValue = new StringBuilder(entry.getValue().toString());
            if (bitsOfValue.length() < COUNT_OF_BITS_IN_BYTE * 2) {
                bitsOfValue.insert(0, "1");
            }
            while (bitsOfValue.length() < COUNT_OF_BITS_IN_BYTE * 2) {
                bitsOfValue.insert(0, "0");
            }
            firstByte = (byte) Integer.parseInt(bitsOfValue.substring(0, COUNT_OF_BITS_IN_BYTE), BINARY_SYSTEM);
            bytesFromCodingTable[index++] = firstByte;
            secondByte = (byte) Integer.parseInt(bitsOfValue.substring(COUNT_OF_BITS_IN_BYTE, COUNT_OF_BITS_IN_BYTE * 2), BINARY_SYSTEM);
            bytesFromCodingTable[index++] = secondByte;
        }
        return bytesFromCodingTable;
    }

    /**
     * Create a new file name.
     *
     * @param fileName The name of the file from which the archive is created.
     * @return The new file name.
     */
    private String createNewFileName(String fileName) {
        return fileName + ADDITIONAL_ARCHIVE_EXTENSION;
    }
}