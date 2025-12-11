/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author btoan
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class InputLogger {

    private static final String LOG_FILE = "input_log.txt";

    public static void log(String action, String detail) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            LocalDateTime now = LocalDateTime.now();
            pw.printf("[%s] %-12s | %s%n",
                    now.toString().replace('T', ' '),
                    action,
                    detail);
        } catch (IOException e) {
            ConsoleUI.printWarning("Cannot write log: " + e.getMessage());
        }
    }

    public static void log(String message) {
        log("INFO", message);
    }
}

