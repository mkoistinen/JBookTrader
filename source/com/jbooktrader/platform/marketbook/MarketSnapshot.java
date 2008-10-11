package com.jbooktrader.platform.marketbook;


/**
 */
public class MarketSnapshot {
    private final long time;
    private final int balance;
    private final double bestBid, bestAsk;

    public MarketSnapshot(long time, int balance, double bestBid, double bestAsk) {
        this.time = time;
        this.balance = balance;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
    }

    public int getBalance() {
        return balance;
    }

    public long getTime() {
        return time;
    }

    public double getBestBid() {
        return bestBid;
    }

    public double getBestAsk() {
        return bestAsk;
    }

    public double getMidPrice() {
        return (bestBid + bestAsk) / 2;
    }


    public String toString() {
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(getTime());
        marketDepth.append(" balance: ").append(balance);
        marketDepth.append(" best bid: ").append(bestBid);
        marketDepth.append(" best ask: ").append(bestAsk);

        return marketDepth.toString();
    }

}
