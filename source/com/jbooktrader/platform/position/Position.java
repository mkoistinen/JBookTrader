package com.jbooktrader.platform.position;

/**
 * Encapsulates the strategy position information
 */
public class Position {
    private final int position;
    private final long date;
    private final double avgFillPrice;

    public Position(long date, int position, double avgFillPrice) {
        this.date = date;
        this.position = position;
        this.avgFillPrice = avgFillPrice;
    }

    public int getPosition() {
        return position;
    }

    public long getDate() {
        return date;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }
}
