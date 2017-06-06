package com.shpp.dbondarenko;

/**
 * File: com.shpp.dbondarenko.Main.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Main {
    public static void main(String[] args) {
        Archiver archiver = new Archiver();
        archiver.createArchive("test.txt");
    }
}
