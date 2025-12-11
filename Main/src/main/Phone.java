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
public class Phone extends Product {
    private boolean support5G;

    public Phone() {
    }

    public Phone(String id, String name, String brand,
                 double price, int quantity, boolean active,
                 boolean support5G) {
        super(id, name, brand, price, quantity, active);
        this.support5G = support5G;
    }

    public boolean isSupport5G() {
        return support5G;
    }

    public void setSupport5G(boolean support5G) {
        this.support5G = support5G;
    }

    @Override
    public String getType() {
        return "Phone";
    }

    @Override
    public String getExtraDataString() {
        return String.valueOf(support5G);
    }

    @Override
    protected double getDiscountRate() {
        return 0.05; // Phone giảm 5%
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" (support5G=%b)", support5G);
    }
}
