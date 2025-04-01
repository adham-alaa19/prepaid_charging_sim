package com.iti.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.iti.models.CallDetailsRecord;

public class CDR_Generator {
   // private static final String FILE_PATH = "/tmp/calls.cdr";
	 private static final String FILE_PATH = "C:\\Users\\theda\\Desktop\\chargProj\\tmp\\cdr.txt";
    public static synchronized void writeCDR(CallDetailsRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(record.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CDR: " + e.getMessage());
        }
    }

    public static void ensureFileExists() {
        try {
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            if (!Files.exists(Paths.get(FILE_PATH))) {
                Files.createFile(Paths.get(FILE_PATH));
            }
        } catch (IOException e) {
            System.err.println("Failed to create CDR file: " + e.getMessage());
        }
    }
}

