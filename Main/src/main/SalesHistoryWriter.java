package main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class SalesHistoryWriter {

    private static final String FILE_NAME = "sales_history.txt";

    public static void recordSale(LocalDateTime time,
                                  Product product,
                                  int quantity,
                                  double originAmount,
                                  double productDiscount,
                                  double finalAmount) {

        double totalDiscount = originAmount - finalAmount;

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            pw.printf(
                "%s | ID=%s | Type=%s | Name=%s | Qty=%d | Origin=%.2f | ProdDiscount=%.2f | TotalDiscount=%.2f | Final=%.2f%n",
                time.toString().replace('T', ' '),
                product.getId(),
                product.getType(),
                product.getName(),
                quantity,
                originAmount,
                productDiscount,
                totalDiscount,
                finalAmount
            );
        } catch (IOException e) {
            ConsoleUI.printWarning("Cannot write sales history: " + e.getMessage());
            InputLogger.log("SALES_HISTORY_IO_ERROR", e.getMessage());
        } catch (Exception e) {
            ConsoleUI.printWarning("Unexpected error when writing sales history: " + e.getMessage());
            InputLogger.log("SALES_HISTORY_EXCEPTION", e.toString());
        }
    }
}
