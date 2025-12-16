package main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class InputLogger {

    private static final String LOG_FILE = "input_log.txt";

    public static void log(String action, String detail) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            LocalDateTime now = LocalDateTime.now();
            pw.printf("[%s] %-20s | %s%n",
                    now.toString().replace('T', ' '),
                    action,
                    detail);
        } catch (IOException e) {
            System.err.println("Could not write log: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected log error: " + e.getMessage());
        }
    }

    public static void log(String message) {
        log("INFO", message);
    }
}
