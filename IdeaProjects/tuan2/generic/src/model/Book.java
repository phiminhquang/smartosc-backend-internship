package model;

public class Book extends Product {
    private String author;

    public Book(String id, String name, double price, String author) {
        super(id, name, price);
        this.author = author;
    }

    @Override
    public String toString() {
        return super.toString() + " (Tác giả: " + author + ")";
    }
}