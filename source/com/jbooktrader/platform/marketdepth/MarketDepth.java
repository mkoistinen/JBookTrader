package com.jbooktrader.platform.marketdepth;


/**
 */
public class MarketDepth {
    private final long time;
    private final int openBalance, highBalance, lowBalance, closeBalance;
    private final double highPrice, lowPrice;

    public MarketDepth(long time, int openBalance, int highBalance, int lowBalance, int closeBalance, double highPrice, double lowPrice) {
        this.time = time;
        this.openBalance = openBalance;
        this.highBalance = highBalance;
        this.lowBalance = lowBalance;
        this.closeBalance = closeBalance;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
    }

    public MarketDepth(int open, int high, int low, int close, double highPrice, double lowPrice) {
        this(System.currentTimeMillis(), open, high, low, close, highPrice, lowPrice);
    }

    public int getOpenBalance() {
        return openBalance;
    }

    public int getHighBalance() {
        return highBalance;
    }

    public int getLowBalance() {
        return lowBalance;
    }

    public int getCloseBalance() {
        return closeBalance;
    }

    public int getMidBalance() {
        return (highBalance + lowBalance) / 2;
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
        marketDepth.append(" open balance: ").append(openBalance);
        marketDepth.append(" high balance: ").append(highBalance);
        marketDepth.append(" low balance: ").append(lowBalance);
        marketDepth.append(" close balance: ").append(closeBalance);
        marketDepth.append(" high price: ").append(highPrice);
        marketDepth.append(" low price: ").append(lowPrice);

        return marketDepth.toString();
    }

}
