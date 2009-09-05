package com.jbooktrader.platform.marketbook;


/**
 */
public class MarketSnapshot {
    private final long time;
    private final double balance;
    private final double price;


    public MarketSnapshot(long time, double balance, double price) {
        this.time = time;
        this.balance = balance;
        this.price = price;
    }

    public double getBalance() {
        return balance;
    }

    public long getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(time);
        marketDepth.append(" balance: ").append(balance);
        marketDepth.append(" price: ").append(price);

        return marketDepth.toString();
    }

}
