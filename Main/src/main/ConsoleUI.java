/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 * Lớp tiện ích hiển thị giao diện console (màu sắc, khung, title, section...)
 * Toàn bộ method đều static, không cần tạo đối tượng.
 */
public class ConsoleUI {

    // ===== ANSI COLORS =====
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";

    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";

    // Chiều rộng cố định cho title (phần nội dung bên trong)
    private static final int TITLE_WIDTH = 40;

    /**
     * Tô màu / style cho chuỗi text.
     * Ví dụ: ConsoleUI.color("OK", ConsoleUI.GREEN, ConsoleUI.BOLD)
     */
    public static String color(String text, String... codes) {
        StringBuilder sb = new StringBuilder();
        for (String c : codes) {
            sb.append(c);
        }
        sb.append(text).append(RESET);
        return sb.toString();
    }

    /**
     * Lặp ký tự c n lần.
     */
    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * In title với khung cố định width = TITLE_WIDTH,
     * chữ được căn giữa.
     */
    public static void printTitle(String title) {
        int contentWidth = TITLE_WIDTH;

        // nếu title dài hơn width cố định thì nới ra cho khỏi bị cắt
        if (title.length() > contentWidth) {
            contentWidth = title.length();
        }

        // line là phần ==== bên trong 2 dấu +
        String line = repeat('=', contentWidth + 2); // +2 cho 2 khoảng trắng 2 bên

        // khung trên: +====...====+
        System.out.println(color("+" + line + "+", CYAN, BOLD));

        // căn giữa title
        int padding = contentWidth - title.length();
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;

        StringBuilder sb = new StringBuilder();
        sb.append("| ");
        for (int i = 0; i < leftPad; i++) sb.append(' ');
        sb.append(title);
        for (int i = 0; i < rightPad; i++) sb.append(' ');
        sb.append(" |");

        System.out.println(color(sb.toString(), CYAN, BOLD));

        // khung dưới
        System.out.println(color("+" + line + "+", CYAN, BOLD));
    }

    /**
     * In tên section (mục) với đường gạch ngang.
     */
    public static void printSection(String label) {
        String text = "─── " + label + " ───────────────────────────────────────";
        System.out.println(color(text, BLUE, BOLD));
    }

    public static void printSuccess(String msg) {
        System.out.println(color("✔ " + msg, GREEN, BOLD));
    }

    public static void printError(String msg) {
        System.out.println(color("✘ " + msg, RED, BOLD));
    }

    public static void printWarning(String msg) {
        System.out.println(color("! " + msg, YELLOW, BOLD));
    }

    public static void printInfo(String msg) {
        System.out.println(color("ℹ " + msg, CYAN));
    }

    /**
     * In header cho menu chính của chương trình.
     */
    public static void printMenuHeader(String systemName) {
        printTitle(systemName);
    }

    /**
     * In một dòng phân cách mỏng.
     */
    public static void printThinLine() {
        System.out.println("──────────────────────────────────────────────────────────────");
    }
}
