package bg.sofia.uni.fmi.mjt.splitwise.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorLogger {
    private static final String LOG_FILE_PATH = "server_errors.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(Throwable e) {
        try (FileWriter fileWriter = new FileWriter(LOG_FILE_PATH, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            printWriter.println("=== Error Timestamp: " + LocalDateTime.now().format(FORMATTER) + " ===");
            printWriter.println("Message: " + e.getMessage());
            e.printStackTrace(printWriter);

            printWriter.println("--------------------------------------------------");
            printWriter.println();

        } catch (IOException ex) {
            System.err.println("Could not write to error log file: " + ex.getMessage());
        }
    }
}
