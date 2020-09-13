package ch.epilibre.epilibre.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {

    private String date;
    private String seller;
    private double totalPrice;
    private boolean hasDiscount;
    private double discountPrice;
    private ArrayList<BasketLine> basketLines;

    public Order(String date, String seller, double totalPrice, boolean hasDiscount, double discountPrice, ArrayList<BasketLine> basketLines) {
        this.date = date;
        this.seller = seller;
        this.totalPrice = totalPrice;
        this.hasDiscount = hasDiscount;
        this.discountPrice = discountPrice;
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

    public boolean hasDiscount() {
        return hasDiscount;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }
}
