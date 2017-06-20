package com.shpp.dbondarenko;

import com.shpp.dbondarenko.archiver.bds.ArchiveToFile;
import com.shpp.dbondarenko.archiver.bds.FileToArchive;
import com.shpp.dbondarenko.archiver.zip.FileToZip;
import com.shpp.dbondarenko.archiver.zip.ZipToFile;

import java.util.Objects;

/**
 * File: com.shpp.dbondarenko.Main.java
 * The сlass, which starts the archiver.
 * Created by Dmitro Bondarenko on 02.06.2017.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Specify the \"bds\" or \"zip\", \"archive\" or \"unarchive\" and name of file.");
        } else {
            if (Objects.equals(args[0], "bds")) {
                if (Objects.equals(args[1], "archive")) {
                    FileToArchive archive = new FileToArchive();
                    archive.createArchiveFromFile(args[2]);
                } else if (Objects.equals(args[1], "unarchive")) {
                    ArchiveToFile archive = new ArchiveToFile();
                    archive.restoreFileFromArchive(args[2]);
                } else {
                    System.out.println("Please, indicate what should be done: \"archive\" or \"unarchive\"");
                }
            } else if (Objects.equals(args[0], "zip")) {
                if (Objects.equals(args[1], "archive")) {
                    FileToZip zipArchive = new FileToZip();
                    zipArchive.createZipFromFile(args[2]);
                } else if (Objects.equals(args[1], "unarchive")) {
                    ZipToFile archive = new ZipToFile();
                    archive.restoreFileFromZip(args[2]);
                } else {
                    System.out.println("Please, indicate what should be done: \"archive\" or \"unarchive\"");
                }
            } else {
                System.out.println("Please indicate which archive type to work with: \"bds\" or \"zip\"");
            }
        }
    }
}

