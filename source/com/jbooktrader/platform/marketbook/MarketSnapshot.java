package com.jbooktrader.platform.marketbook;


/**
 * @author Eugene Kononov
 */
public class MarketSnapshot {
    private final long time;
    private final double balance;
    private final double price;
    private final int volume;


    public MarketSnapshot(long time, double balance, double price, int volume) {
        this.time = time;
        this.balance = balance;
        this.price = price;
        this.volume = volume;
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

    public int getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(time);
        marketDepth.append(" balance: ").append(balance);
        marketDepth.append(" price: ").append(price);
        marketDepth.append(" volume: ").append(volume);

        return marketDepth.toString();
    }

}
