package main;

/**
 * Quản lý danh sách sản phẩm (Laptop, Phone):
 * - Load / save file products.txt
 * - Thêm / sửa / xóa
 * - Bán hàng (invoice + lịch sử sales, thêm giảm giá sinh viên)
 * - Tìm kiếm, filter
 * - Dashboard + export tồn kho
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Comparator;

public class ProductManager {

    private List<Product> products = new ArrayList<>();
    private static final String FILE_NAME = "products.txt";

    // giảm giá thêm cho sinh viên (5%)
    private static final double STUDENT_DISCOUNT_RATE = 0.05;

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
            sortById(); // tự động sort sau khi load
            ConsoleUI.printSuccess("Loaded " + products.size() + " products from file.");
            InputLogger.log("LOAD_FILE", "Loaded " + products.size() + " products.");
        } catch (IOException e) {
            ConsoleUI.printError("Error loading file: " + e.getMessage());
            InputLogger.log("LOAD_FILE_ERROR", e.getMessage());
        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while loading file: " + e.getMessage());
            InputLogger.log("LOAD_FILE_EXCEPTION", e.toString());
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
        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while saving file: " + e.getMessage());
            InputLogger.log("SAVE_FILE_EXCEPTION", e.toString());
        }
    }

    // ===== helper =====
    private Product findById(String id) {
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    private String cutString(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    // luôn sort theo ID tăng dần
    private void sortById() {
        products.sort(Comparator.comparing(p -> p.getId().toLowerCase()));
    }

    // ===== BẢNG PRODUCT CỐ ĐỊNH (ASCII, PRICE RỘNG) =====
    private void printProductTableHeader() {
        ConsoleUI.printSection("PRODUCT LIST (sorted by ID ascending)");
        System.out.println("+----+--------+--------+----------------+----------+--------------+------+--------+--------+");
        System.out.printf("| %-2s | %-6s | %-6s | %-14s | %-8s | %-12s | %-4s | %-6s | %-6s |%n",
                "No", "ID", "Type", "Name", "Brand", "Price", "Qty", "Active", "Extra");
        System.out.println("+----+--------+--------+----------------+----------+--------------+------+--------+--------+");
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

        System.out.printf("| %-2d | %-6s | %-6s | %-14s | %-8s | %12.2f | %-4d | %-6s | %-6s |%n",
                index,
                p.getId(),
                cutString(p.getType(), 6),
                cutString(p.getName(), 14),
                cutString(p.getBrand(), 8),
                p.getPrice(),
                p.getQuantity(),
                activeStr,
                cutString(extra, 6)
        );
    }

    private void printProductTableFooter() {
        System.out.println("+----+--------+--------+----------------+----------+--------------+------+--------+--------+");
    }

    // ===== CRUD =====
    // 1. ADD PRODUCT (0 để quay về)
    public void addProduct(Scanner sc) {
        ConsoleUI.printSection("ADD PRODUCT");
        try {
            System.out.println("1. Laptop   2. Phone   (0 = Back to main menu)");
            System.out.print("Choose type: ");
            String typeStr = sc.nextLine().trim();

            if (typeStr.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }

            int type;
            try {
                type = Integer.parseInt(typeStr);
                if (type != 1 && type != 2) {
                    ConsoleUI.printError("Invalid value for Type (must be 1 or 2). Action canceled.");
                    InputLogger.log("ADD_FAIL_TYPE", "field=type, value=" + typeStr);
                    return;
                }
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid value for Type (must be a number 1 or 2). Action canceled.");
                InputLogger.log("ADD_FAIL_TYPE_FORMAT", "field=type, value=" + typeStr);
                return;
            }

            System.out.print("ID: ");
            String id = sc.nextLine();
            if (findById(id) != null) {
                ConsoleUI.printError("ID already exists! Action canceled.");
                InputLogger.log("ADD_FAIL", "ID=" + id + " already exists");
                return;
            }

            System.out.print("Name: ");
            String name = sc.nextLine();
            System.out.print("Brand: ");
            String brand = sc.nextLine();

            System.out.print("Price: ");
            String priceStr = sc.nextLine().trim();
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid value for Price (must be a number). Action canceled.");
                InputLogger.log("ADD_FAIL_FORMAT", "field=price, value=" + priceStr);
                return;
            }
            if (price < 0) {
                ConsoleUI.printError("Price must be >= 0. Action canceled.");
                InputLogger.log("ADD_FAIL_RANGE", "field=price, value=" + priceStr);
                return;
            }

            System.out.print("Quantity: ");
            String qtyStr = sc.nextLine().trim();
            int quantity;
            try {
                quantity = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid value for Quantity (must be an integer). Action canceled.");
                InputLogger.log("ADD_FAIL_FORMAT", "field=quantity, value=" + qtyStr);
                return;
            }
            if (quantity < 0) {
                ConsoleUI.printError("Quantity must be >= 0. Action canceled.");
                InputLogger.log("ADD_FAIL_RANGE", "field=quantity, value=" + qtyStr);
                return;
            }

            System.out.print("Active (true/false): ");
            String activeStr = sc.nextLine().trim();
            boolean active;
            if (!activeStr.equalsIgnoreCase("true") && !activeStr.equalsIgnoreCase("false")) {
                ConsoleUI.printError("Invalid value for Active (must be true/false). Action canceled.");
                InputLogger.log("ADD_FAIL_FORMAT", "field=active, value=" + activeStr);
                return;
            }
            active = Boolean.parseBoolean(activeStr);

            Product p;
            if (type == 1) {
                System.out.print("Warranty months: ");
                String wStr = sc.nextLine().trim();
                int w;
                try {
                    w = Integer.parseInt(wStr);
                } catch (NumberFormatException e) {
                    ConsoleUI.printError("Invalid value for Warranty months (must be an integer). Action canceled.");
                    InputLogger.log("ADD_FAIL_FORMAT", "field=warrantyMonths, value=" + wStr);
                    return;
                }
                if (w < 0) {
                    ConsoleUI.printError("Warranty months must be >= 0. Action canceled.");
                    InputLogger.log("ADD_FAIL_RANGE", "field=warrantyMonths, value=" + wStr);
                    return;
                }
                p = new Laptop(id, name, brand, price, quantity, active, w);
            } else {
                System.out.print("Support 5G (true/false): ");
                String s5Str = sc.nextLine().trim();
                if (!s5Str.equalsIgnoreCase("true") && !s5Str.equalsIgnoreCase("false")) {
                    ConsoleUI.printError("Invalid value for Support 5G (must be true/false). Action canceled.");
                    InputLogger.log("ADD_FAIL_FORMAT", "field=support5G, value=" + s5Str);
                    return;
                }
                boolean s5 = Boolean.parseBoolean(s5Str);
                p = new Phone(id, name, brand, price, quantity, active, s5);
            }

            products.add(p);
            sortById(); // sort sau khi thêm
            ConsoleUI.printSuccess("Added: " + p);
            InputLogger.log("ADD", p.toDataLine());

        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while adding product: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("ADD_EXCEPTION", e.toString());
        }
    }

    // 2. LIST PRODUCTS (0 để quay về)
    public void listProducts(Scanner sc) {
        try {
            ConsoleUI.printSection("LIST PRODUCTS");
            System.out.print("Press ENTER to show list, or 0 to go back: ");
            String ans = sc.nextLine().trim();
            if (ans.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }

            if (products.isEmpty()) {
                ConsoleUI.printWarning("No products in the list.");
                return;
            }

            // đảm bảo luôn sort trước khi in
            sortById();

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
        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while listing products: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("LIST_EXCEPTION", e.toString());
        }
    }

    // 3. UPDATE PRODUCT (0 để quay về)
    public void updateProduct(Scanner sc) {
        ConsoleUI.printSection("UPDATE PRODUCT");
        try {
            System.out.print("Enter ID to update (0 = Back): ");
            String id = sc.nextLine();
            if (id.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }

            Product p = findById(id);
            if (p == null) {
                ConsoleUI.printError("Product not found. Action canceled.");
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
            String priceStr = sc.nextLine().trim();
            if (!priceStr.isEmpty()) {
                double price;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    ConsoleUI.printError("Invalid value for New price (must be a number). Action canceled.");
                    InputLogger.log("UPDATE_FAIL_FORMAT", "field=price, value=" + priceStr);
                    return;
                }
                if (price < 0 && price != -1) {
                    ConsoleUI.printError("New price must be >= 0 or -1 to skip. Action canceled.");
                    InputLogger.log("UPDATE_FAIL_RANGE", "field=price, value=" + priceStr);
                    return;
                }
                if (price >= 0) p.setPrice(price);
            }

            System.out.print("New quantity (-1 = skip): ");
            String qtyStr = sc.nextLine().trim();
            if (!qtyStr.isEmpty()) {
                int quantity;
                try {
                    quantity = Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    ConsoleUI.printError("Invalid value for New quantity (must be an integer). Action canceled.");
                    InputLogger.log("UPDATE_FAIL_FORMAT", "field=quantity, value=" + qtyStr);
                    return;
                }
                if (quantity < 0 && quantity != -1) {
                    ConsoleUI.printError("New quantity must be >= 0 or -1 to skip. Action canceled.");
                    InputLogger.log("UPDATE_FAIL_RANGE", "field=quantity, value=" + qtyStr);
                    return;
                }
                if (quantity >= 0) p.setQuantity(quantity);
            }

            System.out.print("Active (true/false, blank = skip): ");
            String activeStr = sc.nextLine().trim();
            if (!activeStr.isEmpty()) {
                if (!activeStr.equalsIgnoreCase("true") && !activeStr.equalsIgnoreCase("false")) {
                    ConsoleUI.printError("Invalid value for Active (must be true/false). Action canceled.");
                    InputLogger.log("UPDATE_FAIL_FORMAT", "field=active, value=" + activeStr);
                    return;
                }
                p.setActive(Boolean.parseBoolean(activeStr));
            }

            if (p instanceof Laptop) {
                Laptop lap = (Laptop) p;
                System.out.print("New warranty months (-1 = skip): ");
                String wStr = sc.nextLine().trim();
                if (!wStr.isEmpty()) {
                    int w;
                    try {
                        w = Integer.parseInt(wStr);
                    } catch (NumberFormatException e) {
                        ConsoleUI.printError("Invalid value for New warranty months (must be an integer). Action canceled.");
                        InputLogger.log("UPDATE_FAIL_FORMAT", "field=warrantyMonths, value=" + wStr);
                        return;
                    }
                    if (w < 0 && w != -1) {
                        ConsoleUI.printError("New warranty months must be >= 0 or -1 to skip. Action canceled.");
                        InputLogger.log("UPDATE_FAIL_RANGE", "field=warrantyMonths, value=" + wStr);
                        return;
                    }
                    if (w >= 0) lap.setWarrantyMonths(w);
                }
            } else if (p instanceof Phone) {
                Phone ph = (Phone) p;
                System.out.print("New support5G (true/false, blank = skip): ");
                String s5 = sc.nextLine().trim();
                if (!s5.isEmpty()) {
                    if (!s5.equalsIgnoreCase("true") && !s5.equalsIgnoreCase("false")) {
                        ConsoleUI.printError("Invalid value for New support5G (must be true/false). Action canceled.");
                        InputLogger.log("UPDATE_FAIL_FORMAT", "field=support5G, value=" + s5);
                        return;
                    }
                    ph.setSupport5G(Boolean.parseBoolean(s5));
                }
            }

            sortById(); // nếu sau này có cho đổi ID thì vẫn an toàn
            ConsoleUI.printSuccess("Updated: " + p);
            InputLogger.log("UPDATE", p.toDataLine());

        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while updating product: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("UPDATE_EXCEPTION", e.toString());
        }
    }

    // 4. DELETE PRODUCT (0 để quay về, in info item xóa)
    public void deleteProduct(Scanner sc) {
        ConsoleUI.printSection("DELETE PRODUCT");
        try {
            System.out.print("Enter ID to delete (0 = Back): ");
            String id = sc.nextLine();
            if (id.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }

            Product p = findById(id);
            if (p == null) {
                ConsoleUI.printError("Product not found. Action canceled.");
                InputLogger.log("DELETE_FAIL", "ID=" + id + " not found");
                return;
            }

            // in thông tin item bị xóa
            ConsoleUI.printInfo("Deleting item:");
            System.out.println("  " + p.toString());

            products.remove(p);
            sortById(); // sort lại sau khi xóa

            ConsoleUI.printSuccess("Deleted.");
            InputLogger.log("DELETE", p.toDataLine());
        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while deleting product: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("DELETE_EXCEPTION", e.toString());
        }
    }

    // 5. SELL (có giảm giá sinh viên, 0 để quay về)
    public void sellProduct(Scanner sc) {
        ConsoleUI.printSection("SELL PRODUCT");
        try {
            System.out.print("Enter ID to sell (0 = Back): ");
            String id = sc.nextLine();
            if (id.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }

            Product p = findById(id);
            if (p == null) {
                ConsoleUI.printError("Product not found. Action canceled.");
                InputLogger.log("SELL_FAIL", "ID=" + id + " not found");
                return;
            }
            if (!p.isActive()) {
                ConsoleUI.printError("Product is not active. Action canceled.");
                InputLogger.log("SELL_FAIL", "ID=" + id + " not active");
                return;
            }

            System.out.print("Quantity to sell: ");
            String qStr = sc.nextLine().trim();
            int q;
            try {
                q = Integer.parseInt(qStr);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid value for Quantity to sell (must be an integer). Action canceled.");
                InputLogger.log("SELL_FAIL_FORMAT", "field=quantitySell, value=" + qStr);
                return;
            }

            if (q <= 0 || q > p.getQuantity()) {
                ConsoleUI.printError("Invalid quantity (must be >0 and <= current stock). Action canceled.");
                InputLogger.log("SELL_FAIL", "ID=" + id + ", invalid qty=" + q);
                return;
            }

            // hỏi giảm giá sinh viên
            System.out.print("Is student? (y/n, 0 = Back): ");
            String stuAns = sc.nextLine().trim();
            if (stuAns.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }

            boolean isStudent = stuAns.equalsIgnoreCase("y") || stuAns.equalsIgnoreCase("yes");
            String studentId = "";
            if (isStudent) {
                System.out.print("Enter student ID: ");
                studentId = sc.nextLine().trim();
            }

            // TÍNH TIỀN
            double origin = p.getPrice() * q;

            // giá sau discount theo loại sản phẩm (Laptop/Phone)
            double baseFinal = p.getFinalPrice(q);
            double productDiscount = origin - baseFinal;

            double studentDiscount = 0.0;
            if (isStudent) {
                studentDiscount = baseFinal * STUDENT_DISCOUNT_RATE; // thêm 5% trên baseFinal
            }

            double finalAmount = baseFinal - studentDiscount;
            double totalDiscount = productDiscount + studentDiscount;

            LocalDateTime now = LocalDateTime.now();

            // Hóa đơn
            ConsoleUI.printTitle("INVOICE");
            System.out.printf("Date          : %s%n", now.toString().replace('T', ' '));
            System.out.printf("Product       : %s (%s)%n", p.getName(), p.getType());
            System.out.printf("Brand         : %s%n", p.getBrand());
            System.out.printf("Unit price    : %.2f%n", p.getPrice());
            System.out.printf("Quantity      : %d%n", q);
            if (isStudent) {
                System.out.printf("Student ID    : %s%n", studentId.isEmpty() ? "(not provided)" : studentId);
            }
            ConsoleUI.printThinLine();
            System.out.printf("Sub total          : %.2f%n", origin);
            System.out.printf("Product discount   : -%.2f%n", productDiscount);
            System.out.printf("Student discount   : -%.2f%n", studentDiscount);
            ConsoleUI.printThinLine();
            System.out.printf("Total discount     : -%.2f%n", totalDiscount);
            System.out.println(ConsoleUI.color(
                    String.format("TOTAL TO PAY      : %.2f", finalAmount),
                    ConsoleUI.GREEN, ConsoleUI.BOLD));
            ConsoleUI.printThinLine();

            // cập nhật tồn kho
            p.setQuantity(p.getQuantity() - q);
            sortById(); // sort lại sau khi thay đổi quantity (cho thống nhất)
            ConsoleUI.printSuccess("New quantity in stock: " + p.getQuantity());

            // log
            String logDetail = String.format(
                    "id=%s, qty=%d, origin=%.2f, prodDiscount=%.2f, studentDiscount=%.2f, total=%.2f, studentId=%s",
                    p.getId(), q, origin, productDiscount, studentDiscount, finalAmount,
                    (studentId.isEmpty() ? "N/A" : studentId)
            );
            InputLogger.log("SELL", logDetail);

            // ghi lịch sử sale
            SalesHistoryWriter.recordSale(now, p, q, origin, productDiscount, finalAmount);

        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while selling product: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("SELL_EXCEPTION", e.toString());
        }
    }

    // 7. SEARCH + FILTER (0 để quay về)
    public void searchAndFilter(Scanner sc) {
        ConsoleUI.printSection("SEARCH & FILTER");
        try {
            System.out.print("Keyword in name/brand (0 = Back, blank = skip): ");
            String keywordRaw = sc.nextLine().trim();
            if (keywordRaw.equals("0")) {
                ConsoleUI.printInfo("Action canceled. Back to main menu.");
                return;
            }
            String keyword = keywordRaw.toLowerCase();

            System.out.print("Type (1=All, 2=Laptop, 3=Phone): ");
            String tStr = sc.nextLine().trim();
            int t = 1;
            if (!tStr.isEmpty()) {
                try {
                    t = Integer.parseInt(tStr);
                } catch (NumberFormatException e) {
                    ConsoleUI.printError("Invalid value for Type filter (must be 1/2/3). Action canceled.");
                    InputLogger.log("SEARCH_FAIL_FORMAT", "field=typeFilter, value=" + tStr);
                    return;
                }
                if (t < 1 || t > 3) {
                    ConsoleUI.printError("Invalid value for Type filter (must be 1/2/3). Action canceled.");
                    InputLogger.log("SEARCH_FAIL_RANGE", "field=typeFilter, value=" + tStr);
                    return;
                }
            }
            String typeFilter = null;
            if (t == 2) typeFilter = "Laptop";
            else if (t == 3) typeFilter = "Phone";

            System.out.print("Min price (-1 = no limit): ");
            String minStr = sc.nextLine().trim();
            double minPrice = -1;
            if (!minStr.isEmpty()) {
                try {
                    minPrice = Double.parseDouble(minStr);
                } catch (NumberFormatException e) {
                    ConsoleUI.printError("Invalid value for Min price (must be a number). Action canceled.");
                    InputLogger.log("SEARCH_FAIL_FORMAT", "field=minPrice, value=" + minStr);
                    return;
                }
            }

            System.out.print("Max price (-1 = no limit): ");
            String maxStr = sc.nextLine().trim();
            double maxPrice = -1;
            if (!maxStr.isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(maxStr);
                } catch (NumberFormatException e) {
                    ConsoleUI.printError("Invalid value for Max price (must be a number). Action canceled.");
                    InputLogger.log("SEARCH_FAIL_FORMAT", "field=maxPrice, value=" + maxStr);
                    return;
                }
            }

            if (minPrice >= 0 && maxPrice >= 0 && minPrice > maxPrice) {
                ConsoleUI.printError("Min price cannot be greater than Max price. Action canceled.");
                InputLogger.log("SEARCH_FAIL_RANGE", "min=" + minPrice + ", max=" + maxPrice);
                return;
            }

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
            // in kết quả search cũng sort theo ID
            result.sort(Comparator.comparing(p -> p.getId().toLowerCase()));
            printProductTableHeader();
            int idx = 1;
            for (Product p : result) {
                printProductRow(idx++, p);
            }
            printProductTableFooter();

            InputLogger.log("SEARCH", filterInfo + ", found=" + result.size());

        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while searching: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("SEARCH_EXCEPTION", e.toString());
        }
    }

    // ===== DASHBOARD =====
    public void showDashboard() {
        try {
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
            System.out.printf("Total quantity in stock : %d%n", totalItems);
            System.out.printf("Total inventory value   : %.2f%n", totalValue);

            ConsoleUI.printSection("BY TYPE");
            System.out.printf("Laptop: qty=%d, value=%.2f%n", laptopCount, laptopValue);
            System.out.printf("Phone : qty=%d, value=%.2f%n", phoneCount, phoneValue);

            List<Product> sorted = new ArrayList<>(products);
            sorted.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));

            ConsoleUI.printSection("TOP 3 MOST EXPENSIVE");
            int limit = Math.min(3, sorted.size());
            for (int i = 0; i < limit; i++) {
                Product p = sorted.get(i);
                System.out.printf("%d) %s (%s) - price=%.2f, qty=%d%n",
                        i + 1, p.getName(), p.getType(), p.getPrice(), p.getQuantity());
            }
        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while showing dashboard: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("DASHBOARD_EXCEPTION", e.toString());
        }
    }

    // ===== EXPORT INVENTORY SNAPSHOT =====
    public void exportInventorySnapshot() {
        String fileName = "inventory_list.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

            pw.println("CURRENT INVENTORY SNAPSHOT");
            pw.println("================================================================================");
            pw.println("+----+--------+--------+----------------+----------+--------------+------+--------+--------+");
            pw.printf("| %-2s | %-6s | %-6s | %-14s | %-8s | %-12s | %-4s | %-6s | %-6s |%n",
                    "No", "ID", "Type", "Name", "Brand", "Price", "Qty", "Active", "Extra");
            pw.println("+----+--------+--------+----------------+----------+--------------+------+--------+--------+");

            // đảm bảo file export cũng theo thứ tự ID
            sortById();

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

                pw.printf("| %-2d | %-6s | %-6s | %-14s | %-8s | %12.2f | %-4d | %-6s | %-6s |%n",
                        idx++,
                        p.getId(),
                        cutString(p.getType(), 6),
                        cutString(p.getName(), 14),
                        cutString(p.getBrand(), 8),
                        p.getPrice(),
                        p.getQuantity(),
                        activeStr,
                        cutString(extra, 6)
                );
            }

            pw.println("+----+--------+--------+----------------+----------+--------------+------+--------+--------+");
            pw.println("================================================================================");

            ConsoleUI.printSuccess("Exported inventory to file: " + fileName);
            InputLogger.log("EXPORT_INVENTORY", "Exported to " + fileName);

        } catch (IOException e) {
            ConsoleUI.printError("Error exporting inventory: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("EXPORT_INVENTORY_ERROR", e.getMessage());
        } catch (Exception e) {
            ConsoleUI.printError("Unexpected error while exporting inventory: " + e.getMessage() + ". Action canceled.");
            InputLogger.log("EXPORT_INVENTORY_EXCEPTION", e.toString());
        }
    }
}
