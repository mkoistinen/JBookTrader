package com.jbooktrader.platform.marketdepth;

/**
 * @author Eugene Kononov
 */
public class MarketDepthItem {
    private int size;
    private double price;

    public MarketDepthItem(int size, double price) {
        this.size = size;
        this.price = price;
    }

    public int getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}