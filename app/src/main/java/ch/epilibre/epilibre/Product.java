package ch.epilibre.epilibre;

import java.io.Serializable;

public class Product implements Serializable {

    private int id;
    private String name;
    private double price;
    private double stock;
    private String image;
    private String category;
    private String unit;

    public Product(int id, String name, double price, double stock, String image, String category, String unit) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.category = category;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getStock() {
        return stock;
    }

    public String getImage() {
        return image;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }
}
