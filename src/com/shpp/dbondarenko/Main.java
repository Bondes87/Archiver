package com.shpp.dbondarenko;

import java.util.Objects;

/**
 * File: com.shpp.dbondarenko.Main.java
 * The сlass, which starts the archiver.
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Specify the \"archive\" and \"unarchive\"");
        } else {
            if (Objects.equals(args[0], "archive")) {
                FileToArchive archive = new FileToArchive();
                archive.createArchiveFromFile(args[1]);
            } else if (Objects.equals(args[0], "unarchive")) {
                ArchiveToFile archive = new ArchiveToFile();
                archive.restoreFileFromArchive(args[1]);
            } else {
                System.out.println("Please, indicate what should be done: \"archive\" or \"unarchive\"");
            }
        }
    }
}
