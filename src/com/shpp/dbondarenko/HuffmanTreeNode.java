package com.shpp.dbondarenko;

import java.util.ArrayList;

/**
 * File: HuffmanTreeNode.java
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

    public void setBytes(ArrayList<Byte> bytes) {
        this.bytes = bytes;
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

    public void setLeftChild(HuffmanTreeNode leftChild) {
        LeftChild = leftChild;
    }

    public HuffmanTreeNode getRightChild() {
        return RightChild;
    }

    public void setRightChild(HuffmanTreeNode rightChild) {
        RightChild = rightChild;
    }
}
