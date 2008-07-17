package com.jbooktrader.platform.marketdepth;


/**
 */
public class MarketDepth {
    private final long time;
    private final int balance;
    private final double highPrice, lowPrice;

    public MarketDepth(long time, int balance, double highPrice, double lowPrice) {
        this.time = time;
        this.balance = balance;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
    }

    public MarketDepth(int balance, double highPrice, double lowPrice) {
        this(System.currentTimeMillis(), balance, highPrice, lowPrice);
    }

    public int getBalance() {
        return balance;
    }

    public boolean isValid() {
        return highPrice != 0 && lowPrice != 0;
    }

    public long getTime() {
        return time;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }


    public double getMidPrice() {
        return (highPrice + lowPrice) / 2;
    }


    public String toString() {
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(getTime());
        marketDepth.append(" balance: ").append(balance);
        marketDepth.append(" high price: ").append(highPrice);
        marketDepth.append(" low price: ").append(lowPrice);

        return marketDepth.toString();
    }

}
