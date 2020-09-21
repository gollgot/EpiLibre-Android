package ch.epilibre.epilibre.Models;

import java.io.Serializable;

public class Discount implements Serializable {

    private int percent;
    private String info;

    public Discount(int percent, String info) {
        this.percent = percent;
        this.info = info;
    }

    public int getPercent() {
        return percent;
    }

    public String getInfo() {
        return info;
    }
}
