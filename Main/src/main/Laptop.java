
package main;

public class Laptop extends Product{
    private int warrantyMonths;
    
    public Laptop(){
        
    }

    public Laptop(String id, String name, String brand, double price, int quantity, boolean active, int warrantyMonths) {
        super(id, name, brand, price, quantity, active);
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }
    
    @Override
    public String getType(){
        return "Laptop";
    }
    
    @Override
    public String getExtraDataString(){
        return String.valueOf(warrantyMonths);
    }
    
    @Override
    protected double getDiscountRate(){
        return 0.1;
    }
    
    @Override
    public String toString(){
        return super.toString() + String.format(" (warranty=%d months)", warrantyMonths);
    }
}
