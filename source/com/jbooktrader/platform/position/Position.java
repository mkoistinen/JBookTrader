package com.jbooktrader.platform.position;

/**
 * Encapsulates the strategy position information
 *
 * @author Eugene Kononov
 */
public class Position {
    private final int position;
    private final long time;
    private final double avgFillPrice;

    public Position(long time, int position, double avgFillPrice) {
        this.time = time;
        this.position = position;
        this.avgFillPrice = avgFillPrice;
    }

    public int getPosition() {
        return position;
    }

    public long getTime() {
        return time;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }
}
