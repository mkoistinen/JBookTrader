package com.jbooktrader.platform.marketdepth;

import java.util.*;

/**
 * Holds market depth history for a strategy.
 */
public class MarketBook {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final ArrayList<MarketDepth> marketDepths;

    public MarketBook() {
        marketDepths = new ArrayList<MarketDepth>();
    }

    public List<MarketDepth> getAll() {
        return marketDepths;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MarketDepth marketDepth : marketDepths) {
            sb.append(marketDepth).append(LINE_SEP);
        }

        return sb.toString();
    }

    public int size() {
        return marketDepths.size();
    }

    synchronized public void addMarketDepth(MarketDepth marketDepth) {
        marketDepth = new MarketDepth(marketDepth);
        marketDepths.add(marketDepth);
        notifyAll();
    }

    public void add(MarketDepth marketDepth) {
        marketDepths.add(marketDepth);
    }

    synchronized public MarketDepth getMarketDepth(int index) {
        return marketDepths.get(index);
    }

    synchronized public MarketDepth getFirstMarketDepth() {
        MarketDepth marketDepth = null;
        if (!marketDepths.isEmpty()) {
            marketDepth = marketDepths.get(0);
        }
        return marketDepth;
    }

    synchronized public MarketDepth getLastMarketDepth() {
        MarketDepth marketDepth = null;
        if (!marketDepths.isEmpty()) {
            marketDepth = marketDepths.get(marketDepths.size() - 1);
        }
        return marketDepth;
    }


}
