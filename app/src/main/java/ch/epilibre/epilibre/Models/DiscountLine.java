package ch.epilibre.epilibre.Models;

import java.io.Serializable;

public class DiscountLine extends BasketLine implements Serializable {

    private Discount discount;

    public DiscountLine(Product product, double quantity, Discount discount, double price) {
        super(product, quantity, price);
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }

    @Override
    public String getMainInfo() {
        return discount.getInfo();
    }

    @Override
    public String getDetails() {
        return discount.getPercent() + "%";
    }
}
