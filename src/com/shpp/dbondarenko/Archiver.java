package com.shpp.dbondarenko;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * File: com.shpp.dbondarenko.Archiver.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Archiver {
    public void createArchive(String fileName) throws IOException {
        byte[] bytesFromFile = readFileToBytes(fileName);
        fileArchiving(bytesFromFile);
        System.out.println(bytesFromFile.length);
    }

    private void fileArchiving(byte[] bytesFromFile) {
        ArrayList<HafmannTreeNode> treeLeaves = createHafmannTreeLeaves(bytesFromFile);
        for (HafmannTreeNode leaf : treeLeaves) {
            System.out.println(leaf);
        }
        HafmannTreeNode hafmannTreeRoot = buildHuffmanTree(treeLeaves);
        /*HashMap<Byte, Integer> bytesFrequencyOfFile = countBytesFrequency(bytesFromFile);
        HafmannTreeNode hafmannTreeNode = buildHuffmanTree(bytesFrequencyOfFile);
        int count = 0;
        for (Map.Entry<Byte, Integer> entry : bytesFrequencyOfFile.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            count += entry.getValue();
        }
       *//* System.out.println(bytesFrequencyOfFile.size());
        System.out.println(count);*//*
        HashMap<Byte, Integer> sortedBytesFrequency = sortByValue(bytesFrequencyOfFile);
        for (Map.Entry<Byte, Integer> entry : sortedBytesFrequency.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
    }

    private HafmannTreeNode buildHuffmanTree(ArrayList<HafmannTreeNode> treeLeaves) {
        System.out.println("size " + treeLeaves.size());
        while (treeLeaves.size() > 1) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.addAll(treeLeaves.get(0).getBytes());
            bytes.addAll(treeLeaves.get(1).getBytes());
            int frequency = treeLeaves.get(0).getFrequency() + treeLeaves.get(1).getFrequency();
            treeLeaves.add(new HafmannTreeNode(bytes, frequency, treeLeaves.get(0), treeLeaves.get(1)));
            System.out.println(new HafmannTreeNode(bytes, frequency, treeLeaves.get(0), treeLeaves.get(1)));
            System.out.println("size before: " + treeLeaves.size());
            treeLeaves.remove(0);
            treeLeaves.remove(0);
            Collections.sort(treeLeaves);
            System.out.println("size after: " + treeLeaves.size());
        }
        return null;
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

    private byte[] readFileToBytes(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] bytesFromFile = new byte[inputStream.available()];
        inputStream.read(bytesFromFile);
        return bytesFromFile;
    }
}
