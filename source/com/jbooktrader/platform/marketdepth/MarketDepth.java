package com.jbooktrader.platform.marketdepth;


import java.util.*;

/**
 */
public class MarketDepth {
    private final LinkedList<MarketDepthItem> bids, asks;
    private long time;
    private long lastUpdateTime;

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

        time = System.currentTimeMillis();
    }

    public synchronized void reset() {
        bids.clear();
        asks.clear();
    }

    public boolean isValid() {
        return (!bids.isEmpty() && !asks.isEmpty());
    }


    public long getTime() {
        return time;
    }

    public int getBalance() {
        int cumBidSize = getCumulativeBidSize();
        int cumAskSize = getCumulativeAskSize();

        double totalDepth = (cumBidSize + cumAskSize);
        double strength = 100. * (cumBidSize - cumAskSize) / totalDepth;
        return (int) strength;
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
        StringBuilder marketDepth = new StringBuilder();
        marketDepth.append("time: ").append(getTime());
        marketDepth.append(" bids: ");
        for (MarketDepthItem item : bids) {
            marketDepth.append(item.getSize()).append("@").append(item.getPrice()).append(", ");
        }
        marketDepth.append("  asks: ");
        for (MarketDepthItem item : asks) {
            marketDepth.append(item.getSize()).append("@").append(item.getPrice()).append(", ");
        }

        return marketDepth.toString();
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

    public int getCumulativeBidSize() {
        int cumulativeBid = 0;
        for (MarketDepthItem item : bids) {
            cumulativeBid += item.getSize();
        }
        return cumulativeBid;
    }

    public int getCumulativeAskSize() {
        int cumulativeAsk = 0;
        for (MarketDepthItem item : asks) {
            cumulativeAsk += item.getSize();
        }
        return cumulativeAsk;
    }


    synchronized public long getMillisSinceLastUpdate() {
        return (System.nanoTime() - lastUpdateTime) / 1000000;
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
