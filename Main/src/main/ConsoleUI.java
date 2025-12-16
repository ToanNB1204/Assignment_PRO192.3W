/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

public class ConsoleUI {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    private static final int TITLE_WIDTH = 40;

    public static String color(String text, String... codes) {
        StringBuilder sb = new StringBuilder();
        for (String c : codes) {
            sb.append(c);
        }
        sb.append(text).append(RESET);
        return sb.toString();
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static void printTitle(String title) {
        int contentWidth = TITLE_WIDTH;

        if (title.length() > contentWidth) {
            contentWidth = title.length();
        }

        String line = repeat('=', contentWidth + 2);

        System.out.println(color("+" + line + "+", CYAN, BOLD));

        int padding = contentWidth - title.length();
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;

        StringBuilder sb = new StringBuilder();
        sb.append("| ");
        for (int i = 0; i < leftPad; i++) {
            sb.append(' ');
        }
        sb.append(title);
        for (int i = 0; i < rightPad; i++) {
            sb.append(' ');
        }
        sb.append(" |");

        System.out.println(color(sb.toString(), CYAN, BOLD));

        System.out.println(color("+" + line + "+", CYAN, BOLD));
    }

    public static void printSection(String label) {
        String text = "─── " + label + " ──────────────────────────────────────────────────";
        System.out.println(color(text, BLUE, BOLD));
    }

    public static void printSuccess(String msg) {
        System.out.println(color("✔ " + msg, GREEN, BOLD));
    }

    public static void printError(String msg) {
        System.out.println(color("✘ " + msg, RED, BOLD));
    }

    public static void printWarning(String msg) {
        System.out.println(color("! " + msg, RED, BOLD));
    }

    public static void printInfo(String msg) {
        System.out.println(color("ℹ " + msg, CYAN));
    }

    public static void printMenuHeader(String systemName) {
        printTitle(systemName);
    }

    public static void printThinLine() {
        System.out.println("──────────────────────────────────────────────────────────────");
    }
}
