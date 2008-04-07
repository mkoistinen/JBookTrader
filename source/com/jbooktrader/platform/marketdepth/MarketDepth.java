package com.jbooktrader.platform.marketdepth;


/**
 */
public class MarketDepth {
    private final long time;
    private final int cumulativeBidSize, cumulativeAskSize;
    private final double bid, ask;
    private final int balance;

    public MarketDepth(long time, int cumulativeBidSize, int cumulativeAskSize, double bid, double ask) {
        this.time = time;
        this.cumulativeBidSize = cumulativeBidSize;
        this.cumulativeAskSize = cumulativeAskSize;
        this.bid = bid;
        this.ask = ask;
        double totalDepth = cumulativeBidSize + cumulativeAskSize;
        balance = (int) (100. * (cumulativeBidSize - cumulativeAskSize) / totalDepth);
    }

    public int getCumulativeBidSize() {
        return cumulativeBidSize;
    }

    public int getCumulativeAskSize() {
        return cumulativeAskSize;
    }


    public boolean isValid() {
        return bid != 0 && ask != 0;
    }


    public long getTime() {
        return time;
    }

    public int getBalance() {
        return balance;
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
        marketDepth.append(" cumulative bid size: ").append(cumulativeBidSize);
        marketDepth.append(" cumulative ask size: ").append(cumulativeAskSize);
        marketDepth.append(" bid: ").append(bid);
        marketDepth.append(" ask: ").append(ask);

        return marketDepth.toString();
    }

}
