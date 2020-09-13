package ch.epilibre.epilibre.Models;

import java.io.Serializable;

public class DiscountLine extends BasketLine implements Serializable {

    private int percent;

    public DiscountLine(Product product, double quantity, int percent, double price) {
        super(product, quantity, price);
        this.percent = percent;
    }

    public int getPercent() {
        return percent;
    }

    @Override
    public String getMainInfo() {
        return "Rabais collaborateur";
    }

    @Override
    public String getDetails() {
        return percent + "%";
    }
}
