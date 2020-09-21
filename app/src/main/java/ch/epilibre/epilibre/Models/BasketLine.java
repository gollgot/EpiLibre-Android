package ch.epilibre.epilibre.Models;

import java.io.Serializable;

public class BasketLine implements Serializable {

    private Product product;
    private double quantity;
    private double price;

    public BasketLine(Product product, double quantity, double price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getMainInfo(){
        return product.getName() + " (" + product.getPrice() + " CHF / " + product.getUnit() + ")";
    }

    public String getDetails(){
        return "x " + quantity + " " + product.getUnit();
    }
}
