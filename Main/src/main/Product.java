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
public abstract class Product implements Discountable {

    private String id;
    private String name;
    private String brand;
    private double price;
    private int quantity;
    private boolean active;

    public Product() {
    }

    public Product(String id, String name, String brand, double price, int quantity, boolean active) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    
    
    public abstract String getType();

    public abstract String getExtraDataString();

    protected abstract double getDiscountRate();

    @Override
    public double getFinalPrice(int quantity) {
        double total = price * quantity;
        double discount = total * getDiscountRate();
        return total - discount;
    }

    public String toDataLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(getType()).append(";");
        sb.append(id).append(";");
        sb.append(name).append(";");
        sb.append(brand).append(";");
        sb.append(price).append(";");
        sb.append(quantity).append(";");
        sb.append(active).append(";");
        sb.append(getExtraDataString());
        return sb.toString();
    }

    public static Product fromDataLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";");
        if (parts.length < 8) {
            return null;
        }

        String type = parts[0];
        String id = parts[1];
        String name = parts[2];
        String brand = parts[3];
        double price = Double.parseDouble(parts[4]);
        int qty = Integer.parseInt(parts[5]);
        boolean active = Boolean.parseBoolean(parts[6]);
        String extra = parts[7];

        if ("Laptop".equalsIgnoreCase(type)) {
            int warranty = Integer.parseInt(extra);
            return new Laptop(id, name, brand, price, qty, active, warranty);
        } else if ("Phone".equalsIgnoreCase(type)) {
            boolean support5G = Boolean.parseBoolean(extra);
            return new Phone(id, name, brand, price, qty, active, support5G);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("[%s] id=%s, name=%s, brand=%s, price=%.2f, qty=%d, active=%b, extra=%s",
                getType(), id, name, brand, price, quantity, active, getExtraDataString());
    }
}
