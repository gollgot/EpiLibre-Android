package ch.epilibre.epilibre.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {

    private String date;
    private String seller;
    private double totalPrice;
    private ArrayList<BasketLine> basketLines;

    public Order(String date, String seller, double totalPrice, ArrayList<BasketLine> basketLines) {
        this.date = date;
        this.seller = seller;
        this.totalPrice = totalPrice;
        this.basketLines = basketLines;
    }

    /* GETTERS */

    public String getDate() {
        return date;
    }

    public String getSeller() {
        return seller;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public ArrayList<BasketLine> getBasketLines() {
        return basketLines;
    }
}
