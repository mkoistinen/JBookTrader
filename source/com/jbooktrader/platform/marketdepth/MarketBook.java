package com.jbooktrader.platform.marketdepth;

import java.util.LinkedList;

/**
 * Holds market depth history for a strategy.
 */
public class MarketBook {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepth> marketDepths;

    public MarketBook() {
        marketDepths = new LinkedList<MarketDepth>();
    }

    public LinkedList<MarketDepth> getAll() {
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

    public boolean isEmpty() {
        return marketDepths.size() == 0;
    }

    synchronized public void addMarketDepth(MarketDepth marketDepth) {
        MarketDepth md = new MarketDepth(marketDepth);
        marketDepths.add(md);
    }

    public void add(MarketDepth marketDepth) {
        marketDepths.add(marketDepth);
    }

    synchronized public MarketDepth getMarketDepth(int index) {
        return marketDepths.get(index);
    }

    synchronized public MarketDepth getFirstMarketDepth() {
        return marketDepths.getFirst();
    }

    public MarketDepth getLastMarketDepth() {
        return marketDepths.getLast();
    }


}
