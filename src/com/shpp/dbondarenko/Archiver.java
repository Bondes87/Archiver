package com.shpp.dbondarenko;

import java.io.FileInputStream;
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
        // System.out.println("treeLeaves: " + treeLeaves.size());
        HafmannTreeNode hafmannTreeRoot = buildHuffmanTree(treeLeaves);
        // System.out.println("treeLeaves: " + treeLeaves.size());
        //  System.out.println("hafmannTreeRoot: " + hafmannTreeRoot);
        HashMap<Byte, String> hafmannTable = createHafmannTable(treeLeaves, hafmannTreeRoot);
        for (Map.Entry entry : hafmannTable.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
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

    private byte[] readFileToBytes(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] bytesFromFile = new byte[inputStream.available()];
        inputStream.read(bytesFromFile);
        return bytesFromFile;
    }
}
