package com.shpp.dbondarenko.model;

import java.util.ArrayList;

/**
 * File: HuffmanTreeNode.java
 * The class which is the model of the node of the Huffman tree.
 * Created by Dmitro Bondarenko on 05.06.2017.
 */
public class HuffmanTreeNode implements Comparable<HuffmanTreeNode> {
    private ArrayList<Byte> bytes;
    private int frequency;
    private HuffmanTreeNode LeftChild;
    private HuffmanTreeNode RightChild;

    public HuffmanTreeNode(ArrayList<Byte> bytes, int frequency, HuffmanTreeNode leftChild, HuffmanTreeNode rightChild) {
        this.bytes = bytes;
        this.frequency = frequency;
        LeftChild = leftChild;
        RightChild = rightChild;
    }

    @Override
    public String toString() {
        return "HuffmanTreeNode{" +
                "bytes=" + bytes +
                ", frequency=" + frequency +
                ", LeftChild=" + LeftChild +
                ", RightChild=" + RightChild +
                '}';
    }

    @Override
    public int compareTo(HuffmanTreeNode o) {
        if (o.getFrequency() > frequency) {
            return -1;
        } else if (o.getFrequency() < frequency) {
            return 1;
        } else {
            return 0;
        }
    }

    public ArrayList<Byte> getBytes() {
        return bytes;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public HuffmanTreeNode getLeftChild() {
        return LeftChild;
    }

    public HuffmanTreeNode getRightChild() {
        return RightChild;
    }
}
