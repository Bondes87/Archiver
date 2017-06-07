package com.shpp.dbondarenko;

import java.util.Objects;

/**
 * File: com.shpp.dbondarenko.Main.java
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Main {
    public static void main(String[] args) {
        args = new String[]{"unarchive", "Ш++5.txt-bds"};
        if (args.length < 2) {
            System.out.println("Specify the \"archive\" and \"unarchive\"");
        } else {
            if (Objects.equals(args[0], "archive")) {
                FileToArchive archive = new FileToArchive();
                archive.createArchive(args[1]);
            }
            if (Objects.equals(args[0], "unarchive")) {
                ArchiveToFile archive = new ArchiveToFile();
                archive.unarchive(args[1]);
            }
        }
    }
}
