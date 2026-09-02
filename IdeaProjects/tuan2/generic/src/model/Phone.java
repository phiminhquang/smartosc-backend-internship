package model;

public class Phone extends Product {
    private String ram;

    public Phone(String id, String name, double price, String ram) {
        super(id, name, price);
        this.ram = ram;
    }

    @Override
    public String toString() {
        return super.toString() + " (RAM: " + ram + ")";
    }
}