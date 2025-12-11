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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;

public class ProductManager{

    private List<Product> products = new ArrayList<>();
    private static final String FILE_NAME = "products.txt";

    // ===== FILE I/O =====
    public void loadFromFile() {
        products.clear();
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            ConsoleUI.printInfo("Data file not found, starting with empty list.");
            InputLogger.log("LOAD_FILE", "File not found, start empty.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                Product p = Product.fromDataLine(line);
                if (p != null) products.add(p);
            }
            ConsoleUI.printSuccess("Loaded " + products.size() + " products from file.");
            InputLogger.log("LOAD_FILE", "Loaded " + products.size() + " products.");
        } catch (IOException e) {
            ConsoleUI.printError("Error loading file: " + e.getMessage());
            InputLogger.log("LOAD_FILE_ERROR", e.getMessage());
        }
    }

    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Product p : products) {
                pw.println(p.toDataLine());
            }
            ConsoleUI.printSuccess("Saved " + products.size() + " products to file.");
            InputLogger.log("SAVE_FILE", "Saved " + products.size() + " products.");
        } catch (IOException e) {
            ConsoleUI.printError("Error saving file: " + e.getMessage());
            InputLogger.log("SAVE_FILE_ERROR", e.getMessage());
        }
    }

    // ===== helper =====
    private Product findById(String id) {
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    // ===== TABLE helper =====
    private void printProductTableHeader() {
        ConsoleUI.printSection("PRODUCT LIST");
        System.out.println("┌────┬──────────┬────────┬────────────────────┬────────────┬────────────┬──────┬────────┬────────────┐");
        System.out.printf ("│ %-2s │ %-8s │ %-6s │ %-18s │ %-10s │ %-10s │ %-4s │ %-6s │ %-10s │\n",
                "No", "ID", "Type", "Name", "Brand", "Price", "Qty", "Active", "Extra");
        System.out.println("├────┼──────────┼────────┼────────────────────┼────────────┼────────────┼──────┼────────┼────────────┤");
    }

    private void printProductRow(int index, Product p) {
        String extra;
        if (p instanceof Laptop) {
            extra = "W:" + ((Laptop) p).getWarrantyMonths() + "m";
        } else if (p instanceof Phone) {
            extra = "5G:" + (((Phone) p).isSupport5G() ? "Yes" : "No");
        } else {
            extra = p.getExtraDataString();
        }

        String activeStr = p.isActive() ? "Yes" : "No";

        System.out.printf("│ %-2d │ %-8s │ %-6s │ %-18s │ %-10s │ %10.1f │ %-4d │ %-6s │ %-10s │\n",
                index,
                p.getId(),
                p.getType(),
                cutString(p.getName(), 20),
                cutString(p.getBrand(), 10),
                p.getPrice(),
                p.getQuantity(),
                activeStr,
                extra
        );
    }

    private void printProductTableFooter() {
        System.out.println("└────┴──────────┴────────┴────────────────────┴────────────┴────────────┴──────┴────────┴────────────┘");
    }

    private String cutString(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    // ===== CRUD =====
    public void addProduct(Scanner sc) {
        ConsoleUI.printSection("ADD PRODUCT");
        System.out.println("1. Laptop   2. Phone");
        System.out.print("Choose type: ");
        int type = Integer.parseInt(sc.nextLine());

        System.out.print("ID: ");
        String id = sc.nextLine();
        if (findById(id) != null) {
            ConsoleUI.printError("ID already exists!");
            InputLogger.log("ADD_FAIL", "ID=" + id + " already exists");
            return;
        }

        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Brand: ");
        String brand = sc.nextLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Quantity: ");
        int quantity = Integer.parseInt(sc.nextLine());
        System.out.print("Active (true/false): ");
        boolean active = Boolean.parseBoolean(sc.nextLine());

        Product p;
        if (type == 1) {
            System.out.print("Warranty months: ");
            int w = Integer.parseInt(sc.nextLine());
            p = new Laptop(id, name, brand, price, quantity, active, w);
        } else {
            System.out.print("Support 5G (true/false): ");
            boolean s5 = Boolean.parseBoolean(sc.nextLine());
            p = new Phone(id, name, brand, price, quantity, active, s5);
        }

        products.add(p);
        ConsoleUI.printSuccess("Added: " + p);
        InputLogger.log("ADD", p.toDataLine());
    }

    public void listProducts() {
        if (products.isEmpty()) {
            ConsoleUI.printWarning("No products in the list.");
            return;
        }

        printProductTableHeader();
        int idx = 1;
        for (Product p : products) {
            printProductRow(idx++, p);
        }
        printProductTableFooter();

        // low stock warning
        ConsoleUI.printSection("LOW STOCK WARNING (< 3 items)");
        boolean anyLow = false;
        for (Product p : products) {
            if (p.getQuantity() > 0 && p.getQuantity() < 3) {
                anyLow = true;
                ConsoleUI.printWarning(
                        String.format("%s (%s) only %d left!", p.getName(), p.getId(), p.getQuantity()));
            }
        }
        if (!anyLow) {
            ConsoleUI.printInfo("No low stock items.");
        }
    }

    public void updateProduct(Scanner sc) {
        ConsoleUI.printSection("UPDATE PRODUCT");
        System.out.print("Enter ID to update: ");
        String id = sc.nextLine();
        Product p = findById(id);
        if (p == null) {
            ConsoleUI.printError("Product not found.");
            InputLogger.log("UPDATE_FAIL", "ID=" + id + " not found");
            return;
        }

        System.out.println("Current: " + p);

        System.out.print("New name (blank = skip): ");
        String name = sc.nextLine();
        if (!name.isEmpty()) p.setName(name);

        System.out.print("New brand (blank = skip): ");
        String brand = sc.nextLine();
        if (!brand.isEmpty()) p.setBrand(brand);

        System.out.print("New price (-1 = skip): ");
        double price = Double.parseDouble(sc.nextLine());
        if (price >= 0) p.setPrice(price);

        System.out.print("New quantity (-1 = skip): ");
        int quantity = Integer.parseInt(sc.nextLine());
        if (quantity >= 0) p.setQuantity(quantity);

        System.out.print("Active (true/false, blank = skip): ");
        String activeStr = sc.nextLine();
        if (!activeStr.isEmpty()) p.setActive(Boolean.parseBoolean(activeStr));

        if (p instanceof Laptop) {
            Laptop lap = (Laptop) p;
            System.out.print("New warranty months (-1 = skip): ");
            int w = Integer.parseInt(sc.nextLine());
            if (w >= 0) lap.setWarrantyMonths(w);
        } else if (p instanceof Phone) {
            Phone ph = (Phone) p;
            System.out.print("New support5G (true/false, blank = skip): ");
            String s5 = sc.nextLine();
            if (!s5.isEmpty()) ph.setSupport5G(Boolean.parseBoolean(s5));
        }

        ConsoleUI.printSuccess("Updated: " + p);
        InputLogger.log("UPDATE", p.toDataLine());
    }

    public void deleteProduct(Scanner sc) {
        ConsoleUI.printSection("DELETE PRODUCT");
        System.out.print("Enter ID to delete: ");
        String id = sc.nextLine();
        Product p = findById(id);
        if (p == null) {
            ConsoleUI.printError("Product not found.");
            InputLogger.log("DELETE_FAIL", "ID=" + id + " not found");
            return;
        }
        products.remove(p);
        ConsoleUI.printSuccess("Deleted.");
        InputLogger.log("DELETE", p.toDataLine());
    }

    // ===== SELL =====
    public void sellProduct(Scanner sc) {
        ConsoleUI.printSection("SELL PRODUCT");
        System.out.print("Enter ID to sell: ");
        String id = sc.nextLine();
        Product p = findById(id);
        if (p == null) {
            ConsoleUI.printError("Product not found.");
            InputLogger.log("SELL_FAIL", "ID=" + id + " not found");
            return;
        }
        if (!p.isActive()) {
            ConsoleUI.printError("Product is not active.");
            InputLogger.log("SELL_FAIL", "ID=" + id + " not active");
            return;
        }

        System.out.print("Quantity to sell: ");
        int q = Integer.parseInt(sc.nextLine());
        if (q <= 0 || q > p.getQuantity()) {
            ConsoleUI.printError("Invalid quantity.");
            InputLogger.log("SELL_FAIL", "ID=" + id + ", invalid qty=" + q);
            return;
        }

        double origin = p.getPrice() * q;
        double finalAmount = p.getFinalPrice(q); // discount theo loại sản phẩm
        double totalDiscount = origin - finalAmount;
        double productDiscount = totalDiscount;   // hiện tại toàn bộ discount do Product

        LocalDateTime now = LocalDateTime.now();

        // Hóa đơn
        ConsoleUI.printTitle("INVOICE");
        System.out.printf("Date     : %s\n", now.toString().replace('T', ' '));
        System.out.printf("Product  : %s (%s)\n", p.getName(), p.getType());
        System.out.printf("Brand    : %s\n", p.getBrand());
        System.out.printf("Unit     : %.2f\n", p.getPrice());
        System.out.printf("Quantity : %d\n", q);
        ConsoleUI.printThinLine();
        System.out.printf("Sub total : %.2f\n", origin);
        System.out.printf("Discount  : -%.2f\n", totalDiscount);
        ConsoleUI.printThinLine();
        System.out.println(ConsoleUI.color(
                String.format("TOTAL    : %.2f", finalAmount),
                ConsoleUI.GREEN, ConsoleUI.BOLD));
        ConsoleUI.printThinLine();

        p.setQuantity(p.getQuantity() - q);
        ConsoleUI.printSuccess("New quantity in stock: " + p.getQuantity());

        String logDetail = String.format("id=%s, qty=%d, total=%.2f, discount=%.2f",
                p.getId(), q, finalAmount, totalDiscount);
        InputLogger.log("SELL", logDetail);

        // ghi lịch sử sale
        SalesHistoryWriter.recordSale(now, p, q, origin, productDiscount, finalAmount);
    }

    // ===== SEARCH + FILTER =====
    public void searchAndFilter(Scanner sc) {
        ConsoleUI.printSection("SEARCH & FILTER");

        System.out.print("Keyword in name/brand (blank = skip): ");
        String keyword = sc.nextLine().trim().toLowerCase();

        System.out.print("Type (1=All, 2=Laptop, 3=Phone): ");
        int t = Integer.parseInt(sc.nextLine());
        String typeFilter = null;
        if (t == 2) typeFilter = "Laptop";
        else if (t == 3) typeFilter = "Phone";

        System.out.print("Min price (-1 = no limit): ");
        double minPrice = Double.parseDouble(sc.nextLine());
        System.out.print("Max price (-1 = no limit): ");
        double maxPrice = Double.parseDouble(sc.nextLine());

        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (!keyword.isEmpty()) {
                String target = (p.getName() + " " + p.getBrand()).toLowerCase();
                if (!target.contains(keyword)) continue;
            }
            if (typeFilter != null && !p.getType().equalsIgnoreCase(typeFilter)) continue;
            if (minPrice >= 0 && p.getPrice() < minPrice) continue;
            if (maxPrice >= 0 && p.getPrice() > maxPrice) continue;

            result.add(p);
        }

        String filterInfo = String.format("keyword='%s', type=%s, min=%.2f, max=%.2f",
                keyword, (typeFilter == null ? "ALL" : typeFilter), minPrice, maxPrice);

        if (result.isEmpty()) {
            ConsoleUI.printWarning("No product matched your filters.");
            InputLogger.log("SEARCH_EMPTY", filterInfo);
            return;
        }

        ConsoleUI.printSuccess("Found " + result.size() + " product(s).");
        printProductTableHeader();
        int idx = 1;
        for (Product p : result) {
            printProductRow(idx++, p);
        }
        printProductTableFooter();

        InputLogger.log("SEARCH", filterInfo + ", found=" + result.size());
    }

    // ===== DASHBOARD =====
    public void showDashboard() {
        ConsoleUI.printTitle("INVENTORY DASHBOARD");
        if (products.isEmpty()) {
            ConsoleUI.printWarning("No data.");
            return;
        }

        int totalItems = 0;
        double totalValue = 0;

        int laptopCount = 0, phoneCount = 0;
        double laptopValue = 0, phoneValue = 0;

        for (Product p : products) {
            totalItems += p.getQuantity();
            totalValue += p.getPrice() * p.getQuantity();

            if ("Laptop".equalsIgnoreCase(p.getType())) {
                laptopCount += p.getQuantity();
                laptopValue += p.getPrice() * p.getQuantity();
            } else if ("Phone".equalsIgnoreCase(p.getType())) {
                phoneCount += p.getQuantity();
                phoneValue += p.getPrice() * p.getQuantity();
            }
        }

        ConsoleUI.printSection("SUMMARY");
        System.out.printf("Total quantity in stock : %d\n", totalItems);
        System.out.printf("Total inventory value   : %.2f\n", totalValue);

        ConsoleUI.printSection("BY TYPE");
        System.out.printf("Laptop: qty=%d, value=%.2f\n", laptopCount, laptopValue);
        System.out.printf("Phone : qty=%d, value=%.2f\n", phoneCount, phoneValue);

        List<Product> sorted = new ArrayList<>(products);
        sorted.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));

        ConsoleUI.printSection("TOP 3 MOST EXPENSIVE");
        int limit = Math.min(3, sorted.size());
        for (int i = 0; i < limit; i++) {
            Product p = sorted.get(i);
            System.out.printf("%d) %s (%s) - price=%.2f, qty=%d\n",
                    i + 1, p.getName(), p.getType(), p.getPrice(), p.getQuantity());
        }
    }

    // ===== EXPORT INVENTORY SNAPSHOT =====
    public void exportInventorySnapshot() {
        String fileName = "inventory_list.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

            pw.println("CURRENT INVENTORY SNAPSHOT");
            pw.println("===============================================");
            pw.printf("%-3s | %-8s | %-6s | %-20s | %-10s | %-10s | %-4s | %-6s | %-10s%n",
                    "No", "ID", "Type", "Name", "Brand", "Price", "Qty", "Active", "Extra");
            pw.println("-------------------------------------------------------------------------------------");

            int idx = 1;
            for (Product p : products) {
                String extra;
                if (p instanceof Laptop) {
                    extra = "W:" + ((Laptop) p).getWarrantyMonths() + "m";
                } else if (p instanceof Phone) {
                    extra = "5G:" + (((Phone) p).isSupport5G() ? "Yes" : "No");
                } else {
                    extra = p.getExtraDataString();
                }

                String activeStr = p.isActive() ? "Yes" : "No";

                pw.printf("%-3d | %-8s | %-6s | %-20s | %-10s | %10.2f | %-4d | %-6s | %-10s%n",
                        idx++,
                        p.getId(),
                        p.getType(),
                        p.getName(),
                        p.getBrand(),
                        p.getPrice(),
                        p.getQuantity(),
                        activeStr,
                        extra
                );
            }

            pw.println("-------------------------------------------------------------------------------------");
            ConsoleUI.printSuccess("Exported inventory to file: " + fileName);
            InputLogger.log("EXPORT_INVENTORY", "Exported to " + fileName);

        } catch (IOException e) {
            ConsoleUI.printError("Error exporting inventory: " + e.getMessage());
            InputLogger.log("EXPORT_INVENTORY_ERROR", e.getMessage());
        }
    }
}
