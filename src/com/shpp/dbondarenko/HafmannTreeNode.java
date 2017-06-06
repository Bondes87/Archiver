package com.shpp.dbondarenko;

import java.util.ArrayList;

/**
 * File: HafmannTreeNode.java
 * Created by Dmitro Bondarenko on 05.06.2017.
 */
public class HafmannTreeNode implements Comparable<HafmannTreeNode> {
    private ArrayList<Byte> bytes;
    private int frequency;
    private HafmannTreeNode LeftChild;
    private HafmannTreeNode RightChild;

    public HafmannTreeNode(ArrayList<Byte> bytes, int frequency, HafmannTreeNode leftChild, HafmannTreeNode rightChild) {
        this.bytes = bytes;
        this.frequency = frequency;
        LeftChild = leftChild;
        RightChild = rightChild;
    }

    @Override
    public String toString() {
        return "HafmannTreeNode{" +
                "bytes=" + bytes +
                ", frequency=" + frequency +
                ", LeftChild=" + LeftChild +
                ", RightChild=" + RightChild +
                '}';
    }

    @Override
    public int compareTo(HafmannTreeNode o) {
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

    public HafmannTreeNode getLeftChild() {
        return LeftChild;
    }

    public void setLeftChild(HafmannTreeNode leftChild) {
        LeftChild = leftChild;
    }

    public HafmannTreeNode getRightChild() {
        return RightChild;
    }

    public void setRightChild(HafmannTreeNode rightChild) {
        RightChild = rightChild;
    }
}
