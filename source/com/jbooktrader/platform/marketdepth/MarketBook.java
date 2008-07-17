package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.backtest.*;

import java.io.*;
import java.util.*;

/**
 * Holds market depth history for a strategy.
 */
public class MarketBook {
    private final static int INSERT = 0, UPDATE = 1, DELETE = 2;
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepth> marketDepths;
    private final LinkedList<MarketDepthItem> bids, asks;
    private BackTestFileWriter backTestFileWriter;
    private String name;
    private TimeZone timeZone;
    private int highBalance, lowBalance, lastBalance;
    private double highPrice, lowPrice;

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
            backTestFileWriter = new BackTestFileWriter(name, timeZone, true);
        }
        backTestFileWriter.write(marketDepth, true);
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

    public void add(MarketDepth marketDepth) {
        //todo: reset book and indicators at the start of the day for backtesting and optimization purposes
        marketDepths.add(marketDepth);
    }

    public MarketDepth getLastMarketDepth() {
        return marketDepths.getLast();
    }

    synchronized public void reset() {
        bids.clear();
        asks.clear();
        highPrice = lowPrice = highBalance = lowBalance = lastBalance = 0;
    }

    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }

    synchronized public MarketDepth getNextMarketDepth() {
        MarketDepth marketDepth = null;
        if (lowPrice != 0 && highPrice != 0) {
            int balance = (highBalance + lowBalance) / 2;
            marketDepth = new MarketDepth(balance, highPrice, lowPrice);
            // initialize next market depth values
            highBalance = lowBalance = lastBalance;
            highPrice = asks.getFirst().getPrice();
            lowPrice = bids.getFirst().getPrice();
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
            int cumulativeBid = getCumulativeSize(bids);
            int cumulativeAsk = getCumulativeSize(asks);
            double totalDepth = cumulativeBid + cumulativeAsk;
            lastBalance = (int) (100. * (cumulativeBid - cumulativeAsk) / totalDepth);
            highBalance = Math.max(lastBalance, highBalance);
            lowBalance = Math.min(lastBalance, lowBalance);

            double bestBid = bids.getFirst().getPrice();
            double bestAsk = asks.getFirst().getPrice();
            highPrice = (highPrice == 0) ? bestAsk : Math.max(highPrice, bestAsk);
            lowPrice = (lowPrice == 0) ? bestBid : Math.min(lowPrice, bestBid);
        }
    }
}
