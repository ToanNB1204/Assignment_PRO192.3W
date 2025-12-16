package main;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ProductManager manager = new ProductManager();

        manager.loadFromFile();

        int choice = -1;
        do {
            showMenu();
            System.out.print("Your choice: ");
            String line = sc.nextLine();

            try {
                choice = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid menu choice, please enter a number.");
                InputLogger.log("MENU_CHOICE_FORMAT", "input=" + line);
                choice = -1;
            }

            try {
                switch (choice) {
                    case 1: manager.addProduct(sc);            break;
                    case 2: manager.listProducts(sc);          break;  
                    case 3: manager.updateProduct(sc);         break;
                    case 4: manager.deleteProduct(sc);         break;
                    case 5: manager.sellProduct(sc);           break;
                    case 6: manager.saveToFile();              break;
                    case 7: manager.searchAndFilter(sc);       break;
                    case 8: manager.showDashboard();           break;
                    case 9: manager.exportInventorySnapshot(); break;
                    case 0:
                        manager.saveToFile();
                        ConsoleUI.printSuccess("Bye!");
                        break;
                    default:
                        ConsoleUI.printError("Invalid choice. Please choose from 0-9.");
                }
            } catch (Exception e) {
                ConsoleUI.printError("Unexpected error: " + e.getMessage());
                InputLogger.log("MENU_ACTION_EXCEPTION", e.toString());
            }

            System.out.println();
        } while (choice != 0);

        sc.close();
    }

    private static void showMenu() {
        ConsoleUI.printMenuHeader("PRODUCT MANAGEMENT SYSTEM");
        System.out.println("1. Add product (Laptop / Phone)");
        System.out.println("2. List products");
        System.out.println("3. Update product");
        System.out.println("4. Delete product");
        System.out.println("5. Sell product");
        System.out.println("6. Save to file");
        System.out.println("7. Search & filter products");
        System.out.println("8. Inventory dashboard");
        System.out.println("9. Export inventory to file");
        System.out.println("0. Exit");
        ConsoleUI.printThinLine();
    }
}
