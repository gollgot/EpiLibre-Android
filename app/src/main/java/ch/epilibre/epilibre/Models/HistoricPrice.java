package ch.epilibre.epilibre.Models;

public class HistoricPrice {

    private String productName;
    private double oldPrice;
    private double newPrice;
    private boolean seen;
    private String createdAt;
    private String createdBy;


    public HistoricPrice(String productName, double oldPrice, double newPrice, boolean seen, String createdAt, String createdBy) {
        this.productName = productName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.seen = seen;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }


    // GETTERS

    public String getProductName() {
        return productName;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
