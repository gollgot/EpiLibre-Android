package ch.epilibre.epilibre.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {

    private String date;
    private String seller;
    private double totalPrice;
    private double discountPrice;
    private Discount discount;
    private ArrayList<BasketLine> basketLines;

    public Order(String date, String seller, double totalPrice, double discountPrice, Discount discount, ArrayList<BasketLine> basketLines) {
        this.date = date;
        this.seller = seller;
        this.totalPrice = totalPrice;
        this.discountPrice = discountPrice;
        this.discount = discount;
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
        return discount != null;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public Discount getDiscount() {
        return discount;
    }
}
