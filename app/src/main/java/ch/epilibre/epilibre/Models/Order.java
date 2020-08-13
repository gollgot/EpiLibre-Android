package ch.epilibre.epilibre.Models;

import java.io.Serializable;

public class Order implements Serializable {

    private int id;
    private String date;
    private String seller;
    private double totalPrice;
    private int nbProducts;

    public Order(int id, String date, String seller, double totalPrice, int nbProducts) {
        this.id = id;
        this.date = date;
        this.seller = seller;
        this.totalPrice = totalPrice;
        this.nbProducts = nbProducts;
    }

    /* GETTERS */

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getSeller() {
        return seller;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public int getNbProducts() {
        return nbProducts;
    }
}
