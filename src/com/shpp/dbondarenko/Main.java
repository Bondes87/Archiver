package com.shpp.dbondarenko;

import java.io.IOException;

/**
 * File: com.shpp.dbondarenko.Main.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Main {
    public static void main(String[] args) {
        Archiver archiver = new Archiver();
        try {
            archiver.createArchive("Ш++5.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
