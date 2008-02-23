package com.jbooktrader.platform.marketdepth;


import java.util.*;

/**
 */
public class MarketDepth {
    private final LinkedList<MarketDepthItem> bids, asks;
    private long time;

    public MarketDepth() {
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
    }

    public MarketDepth(LinkedList<MarketDepthItem> bids, LinkedList<MarketDepthItem> asks, long time) {
        this.bids = bids;
        this.asks = asks;
        this.time = time;
    }

    public MarketDepth(MarketDepth marketDepth) {
        this();

        for (MarketDepthItem item : marketDepth.bids) {
            bids.add(new MarketDepthItem(item.getSize(), item.getPrice()));
        }
        for (MarketDepthItem item : marketDepth.asks) {
            asks.add(new MarketDepthItem(item.getSize(), item.getPrice()));
        }
        time = marketDepth.time;
    }

    public synchronized void reset() {
        bids.clear();
        asks.clear();
    }

    // todo: forex depth is sometimes less than 5, so this needs refactoring
    public boolean isValid() {
        return bids.size() >= 5 && asks.size() >= 5;
    }


    public long getTime() {
        return time;
    }

    public double getBestBid() {
        return bids.getFirst().getPrice();
    }

    public double getBestAsk() {
        return asks.getFirst().getPrice();
    }

    public double getMidPoint() {
        return (getBestBid() + getBestAsk()) / 2;
    }

    public LinkedList<MarketDepthItem> getBids() {
        return bids;
    }

    public LinkedList<MarketDepthItem> getAsks() {
        return asks;
    }

    public String toString() {
        String s = "time: " + getTime();
        s += " bids: ";
        for (MarketDepthItem item : bids) {
            s += item.getSize() + "@" + item.getPrice() + ", ";
        }
        s += "  asks: ";
        for (MarketDepthItem item : asks) {
            s += item.getSize() + "@" + item.getPrice() + ", ";
        }

        return s;
    }

    public String toShortString() {
        int cumulativeBid = 0;
        for (MarketDepthItem item : bids) {
            cumulativeBid += item.getSize();
        }
        int cumulativeAsk = 0;
        for (MarketDepthItem item : asks) {
            cumulativeAsk += item.getSize();
        }
        return cumulativeBid + "-" + cumulativeAsk;
    }


    public void update() {
        time = System.currentTimeMillis();
    }

    synchronized public void update(int position, int operation, int side, double price, int size) {
        time = System.currentTimeMillis();
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
