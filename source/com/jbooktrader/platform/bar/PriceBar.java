package com.jbooktrader.platform.bar;

/**
 * Encapsulates the price bar information.
 */
public class PriceBar {
    private long time;
    private double open, high, low, close;

    public PriceBar(long time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public PriceBar(long time, double price) {
        this(time, price, price, price, price);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" time: ").append(time);
        sb.append(" open: ").append(open);
        sb.append(" high: ").append(high);
        sb.append(" low: ").append(low);
        sb.append(" close: ").append(close);
        return sb.toString();
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

}
