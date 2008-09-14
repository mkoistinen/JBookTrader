package com.jbooktrader.platform.marketbook;


/**
 */
public class MarketSnapshot {
    private final long time;
    private final int lowBalance, highBalance;
    private final double bestBid, bestAsk;
    private final int volume;
    private final double tick;

    public MarketSnapshot(long time, int lowBalance, int highBalance, double bestBid, double bestAsk, int volume, double tick) {
        this.time = time;
        this.lowBalance = lowBalance;
        this.highBalance = highBalance;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.volume = volume;
        this.tick = tick;
    }

    public int getLowBalance() {
        return lowBalance;
    }

    public int getHighBalance() {
        return highBalance;
    }

    public int getMidBalance() {
        return (highBalance + lowBalance) / 2;
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

    public int getVolume() {
        return volume;
    }

    public double getTick() {
        return tick;
    }

    public double getMidPrice() {
        return (bestBid + bestAsk) / 2;
    }


    public String toString() {
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(getTime());
        marketDepth.append(" low balance: ").append(lowBalance);
        marketDepth.append(" high balance: ").append(highBalance);
        marketDepth.append(" best bid: ").append(bestBid);
        marketDepth.append(" best ask: ").append(bestAsk);
        marketDepth.append(" volume: ").append(volume);
        marketDepth.append(" tick: ").append(tick);

        return marketDepth.toString();
    }

}

/* $Id$ */
