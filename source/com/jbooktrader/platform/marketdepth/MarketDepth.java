package com.jbooktrader.platform.marketdepth;


/**
 */
public class MarketDepth {
    private final long time;
    private final int lowBalance, highBalance;
    private final double bid, ask;

    public MarketDepth(long time, int lowBalance, int highBalance, double bid, double ask) {
        this.time = time;
        this.lowBalance = lowBalance;
        this.highBalance = highBalance;
        this.bid = bid;
        this.ask = ask;
    }

    public MarketDepth(int lowBalance, int highBalance, double bid, double ask) {
        this(System.currentTimeMillis(), lowBalance, highBalance, bid, ask);
    }


    public int getLowBalance() {
        return lowBalance;
    }

    public int getHighBalance() {
        return highBalance;
    }

    public int getMidBalance() {
        return (lowBalance + highBalance) / 2;
    }

    public boolean isValid() {
        return bid != 0 && ask != 0;
    }


    public long getTime() {
        return time;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double getMidPoint() {
        return (bid + ask) / 2;
    }


    public String toString() {
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(getTime());
        marketDepth.append(" low balance: ").append(lowBalance);
        marketDepth.append(" high balance: ").append(highBalance);
        marketDepth.append(" bid: ").append(bid);
        marketDepth.append(" ask: ").append(ask);

        return marketDepth.toString();
    }

}
