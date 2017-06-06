package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * File: com.shpp.dbondarenko.Archiver.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Archiver {

    private static final String FILE_EXTENSION = ".bds";

    public void createArchive(String fileName) {
        byte[] bytesFromFile = readFileToBytes(fileName);
        System.out.println(bytesFromFile.length);
        HashMap<Byte, String> codingTable = createCodingTable(bytesFromFile);
        byte[] bytesToFile = fileArchiving(bytesFromFile, codingTable);
        for (byte b : bytesFromFile) {
            System.out.println(b);
        }
        writeBytesToFile(bytesToFile, fileName);
        /*for (Map.Entry entry : codingTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
    }

    private void writeBytesToFile(byte[] bytesToFile, String fileName) {
        String name = createFileName(fileName);
        System.out.println(name);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(name);
            outputStream.write(bytesToFile);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Sorry. Create an archive failed.");
            e.printStackTrace();
        }
    }

    private String createFileName(String fileName) {
        String[] nameAndExtension = fileName.split("\\.");
        return nameAndExtension[0] + FILE_EXTENSION;
    }


    private byte[] fileArchiving(byte[] bytesFromFile, HashMap<Byte, String> codingTable) {
        StringBuilder bitSequence = createBitsLine(bytesFromFile, codingTable);
        System.out.println("bitSequence: " + bitSequence);
        System.out.println("bitSequence length: " + bitSequence.length());
        byte[] bytes = getBytes(bitSequence, codingTable);
        return bytes;
    }

    private byte[] getBytes(StringBuilder bitSequence, HashMap<Byte, String> codingTable) {
        ArrayList<Byte> bytesList = new ArrayList<>();
        byte b;
        for (Map.Entry entry : codingTable.entrySet()) {
            if (entry.getKey() != null) {
                b = (Byte) entry.getKey();
                bytesList.add(b);
                //System.out.println(b);
                b = (byte) Integer.parseInt(entry.getValue().toString(), 2);
                bytesList.add(b);
                //System.out.println(b);
            } else {
                b = 0;
                bytesList.add(0, b);
                b = (byte) Integer.parseInt(codingTable.get(null), 2);
                bytesList.add(1, b);
            }
        }
        while (bitSequence.length() > 0) {
            String subString = bitSequence.substring(0, 8);
            b = (byte) Integer.parseInt(subString, 2);
            bytesList.add(b);
            bitSequence = bitSequence.delete(0, 8);
            // System.out.println(b);
        }
        byte[] bytes = fromListToArray(bytesList);
        return bytes;
    }

    private byte[] fromListToArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    private StringBuilder createBitsLine(byte[] bytesFromFile, HashMap<Byte, String> codingTable) {
        StringBuilder bitSequence = new StringBuilder();
        StringBuilder endByte = new StringBuilder();
        for (byte oneByte : bytesFromFile) {
            bitSequence.append(codingTable.get(oneByte));
        }
        int numberOfBitsInLastByte = bitSequence.length() % 8;
        System.out.println(numberOfBitsInLastByte);
        if (numberOfBitsInLastByte != 0) {
            while (numberOfBitsInLastByte != 8) {
                endByte.append("0");
                numberOfBitsInLastByte++;
            }
            bitSequence.append(endByte);
        }
        codingTable.put(null, endByte.toString());
        return bitSequence;
    }

    private HashMap<Byte, String> createCodingTable(byte[] bytesFromFile) {
        ArrayList<HafmannTreeNode> treeLeaves = createHafmannTreeLeaves(bytesFromFile);
        for (HafmannTreeNode leaf : treeLeaves) {
            System.out.println(leaf);
        }
        // System.out.println("treeLeaves: " + treeLeaves.size());
        HafmannTreeNode hafmannTreeRoot = buildHuffmanTree(treeLeaves);
        // System.out.println("treeLeaves: " + treeLeaves.size());
        //  System.out.println("hafmannTreeRoot: " + hafmannTreeRoot);
        HashMap<Byte, String> hafmannTable = createHafmannTable(treeLeaves, hafmannTreeRoot);
        for (Map.Entry entry : hafmannTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        return hafmannTable;
    }


    private HashMap<Byte, String> createHafmannTable(ArrayList<HafmannTreeNode> treeLeaves, HafmannTreeNode hafmannTreeRoot) {
        HashMap<Byte, String> hafmannTable = new HashMap<>();
        System.out.println("treeLeaves: " + treeLeaves.size());
        /*byte oneByte = treeLeaves.get(0).getBytes().get(treeLeaves.get(0).getBytes().size()-1);
        System.out.println(oneByte);
        String idByte = createIdByte(oneByte, hafmannTreeRoot);
        System.out.println(idByte);*/
        for (HafmannTreeNode leaf : treeLeaves) {
            byte oneByte = leaf.getBytes().get(0);
            String idByte = createIdByte(oneByte, hafmannTreeRoot);
            hafmannTable.put(oneByte, idByte);
        }
        System.out.println("hafmannTable: " + hafmannTable.size());
        // System.out.println("treeLeaves: " + treeLeaves.size());
        return hafmannTable;
    }

    private String createIdByte(Byte oneByte, HafmannTreeNode hafmannTreeRoot) {
        String idByte = "";
        if (hafmannTreeRoot != null && hafmannTreeRoot.getBytes().contains(oneByte)) {
            if (hafmannTreeRoot.getLeftChild() != null &&
                    hafmannTreeRoot.getLeftChild().getBytes().contains(oneByte)) {
                idByte = createIdByte(oneByte, hafmannTreeRoot.getLeftChild()) + "0";
            }
            if (hafmannTreeRoot.getRightChild() != null &&
                    hafmannTreeRoot.getRightChild().getBytes().contains(oneByte)) {
                idByte = createIdByte(oneByte, hafmannTreeRoot.getRightChild()) + "1";
            }
        }
        return idByte;
    }

    private HafmannTreeNode buildHuffmanTree(ArrayList<HafmannTreeNode> treeLeaves) {
        ArrayList<HafmannTreeNode> treeNodes = new ArrayList<>(treeLeaves);
        // System.out.println("size " + treeNodes.size());
        while (treeNodes.size() > 1) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.addAll(treeNodes.get(0).getBytes());
            bytes.addAll(treeNodes.get(1).getBytes());
            int frequency = treeNodes.get(0).getFrequency() + treeNodes.get(1).getFrequency();
            treeNodes.add(new HafmannTreeNode(bytes, frequency, treeNodes.get(0), treeNodes.get(1)));
            //System.out.println(new HafmannTreeNode(bytes, frequency, treeNodes.get(0), treeNodes.get(1)));
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
