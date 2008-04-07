package com.jbooktrader.platform.marketdepth;

import java.util.*;

/**
 * Holds market depth history for a strategy.
 */
public class MarketBook {

    private static final long MAX_SIZE = 3 * 60 * 60 * 8;
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepth> marketDepths;
    private final LinkedList<MarketDepthItem> bids, asks;
    private long lastUpdateTime;

    public MarketBook() {
        marketDepths = new LinkedList<MarketDepth>();
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
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

    public void add(MarketDepth marketDepth) {
        marketDepths.add(marketDepth);
        if (marketDepths.size() > MAX_SIZE) {
            marketDepths.removeFirst();
        }
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

    synchronized public void reset() {
        bids.clear();
        asks.clear();
    }

    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }


    synchronized public void signal() {
        long instant = System.nanoTime();
        long millisSinceLastUpdate = (instant - lastUpdateTime) / 1000000;
        if (millisSinceLastUpdate > 100) {
            if (bids.size() > 0 && asks.size() > 0) {
                double bid = bids.getFirst().getPrice();
                double ask = asks.getFirst().getPrice();
                int cumulativeBid = getCumulativeSize(bids);
                int cumulativeAsk = getCumulativeSize(asks);
                add(new MarketDepth(System.currentTimeMillis(), cumulativeBid, cumulativeAsk, bid, ask));
                notifyAll();
            }
        }
    }

    synchronized public void update(int position, int operation, int side, double price, int size) {
        lastUpdateTime = System.nanoTime();

        List<MarketDepthItem> items = (side == 1) ? bids : asks;
        switch (operation) {
            case 0:// insert
                items.add(position, new MarketDepthItem(size, price));
                break;
            case 1:// update
                MarketDepthItem item = items.get(position);
                item.setSize(size);
                item.setPrice(price);
                break;
            case 2:// delete
                if (position < items.size()) {
                    items.remove(position);
                }
                break;
        }
    }
}
