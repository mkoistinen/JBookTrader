package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.backtest.*;

import java.io.*;
import java.util.*;

/**
 * Holds market depth history for a strategy.
 */
public class MarketBook {
    private final static int INSERT = 0, UPDATE = 1, DELETE = 2;
    private static final long MAX_SIZE = 3 * 60 * 60 * 12; // approximately 12 hours
    private static final long QUIET_PERIOD = 100000000; // 100 ms
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepth> marketDepths;
    private final LinkedList<MarketDepthItem> bids, asks;
    private long lastUpdateTime;
    private boolean hasUpdate;
    private BackTestFileWriter backTestFileWriter;
    private String name;
    private TimeZone timeZone;
    private int lowBalance, highBalance;

    public MarketBook() {
        marketDepths = new LinkedList<MarketDepth>();
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void save(MarketDepth marketDepth) throws IOException {
        if (backTestFileWriter == null) {
            backTestFileWriter = new BackTestFileWriter(name, timeZone);
        }
        backTestFileWriter.write(marketDepth, true);
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

    synchronized public void add(MarketDepth marketDepth) {
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

    synchronized public MarketDepth getNewMarketDepth() {
        MarketDepth marketDepth = null;
        long nanosSinceLastUpdate = System.nanoTime() - lastUpdateTime;

        if (hasUpdate && nanosSinceLastUpdate >= QUIET_PERIOD) {
            hasUpdate = false;
            if (!bids.isEmpty() && !asks.isEmpty()) {
                double bid = bids.getFirst().getPrice();
                double ask = asks.getFirst().getPrice();
                marketDepth = new MarketDepth(lowBalance, highBalance, bid, ask);
                lowBalance = 100;
                highBalance = -100;
            }
        }

        return marketDepth;
    }

    synchronized public void update(int position, int operation, int side, double price, int size) {
        List<MarketDepthItem> items = (side == 1) ? bids : asks;
        switch (operation) {
            case INSERT:
                items.add(position, new MarketDepthItem(size, price));
                break;
            case UPDATE:
                MarketDepthItem item = items.get(position);
                item.setSize(size);
                item.setPrice(price);
                break;
            case DELETE:
                if (position < items.size()) {
                    items.remove(position);
                }
                break;
        }

        if (operation == UPDATE) {
            lastUpdateTime = System.nanoTime();
            hasUpdate = true;
            int cumulativeBid = getCumulativeSize(bids);
            int cumulativeAsk = getCumulativeSize(asks);
            double totalDepth = cumulativeBid + cumulativeAsk;
            int balance = (int) (100. * (cumulativeBid - cumulativeAsk) / totalDepth);
            lowBalance = Math.min(balance, lowBalance);
            highBalance = Math.max(balance, highBalance);
        }
    }
}
