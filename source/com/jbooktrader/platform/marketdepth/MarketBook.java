package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.backtest.*;

import java.io.*;
import java.util.*;

/**
 * Holds market depth history for a strategy.
 */
public class MarketBook {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepth> marketDepths;
    private final LinkedList<MarketDepthItem> bids, asks;
    private BackTestFileWriter backTestFileWriter;
    private String name;
    private TimeZone timeZone;
    private double lowBalance, highBalance, lastBalance;
    private int cumulativeVolume, previousCumulativeVolume;

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

    public MarketDepth getPreviousMarketDepth() {
        return marketDepths.get(marketDepths.size() - 2);
    }


    public void reset() {
        bids.clear();
        asks.clear();
    }

    private boolean isValid() {
        return (bids.size() == 5 && asks.size() == 5);
    }

    public int getCumulativeVolume() {
        return cumulativeVolume;
    }


    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }

    public MarketDepth getNextMarketDepth(long time) {
        MarketDepth marketDepth = null;
        if (isValid()) {
            int volume = cumulativeVolume - previousCumulativeVolume;
            double bestBid = bids.getFirst().getPrice();
            double bestAsk = asks.getFirst().getPrice();
            marketDepth = new MarketDepth(time, (int) Math.round(lowBalance), (int) Math.round(highBalance), bestBid, bestAsk, volume);

            // initialize next market depth values
            previousCumulativeVolume = cumulativeVolume;
            highBalance = lowBalance = lastBalance;
        }

        return marketDepth;
    }


    public void update(int cumulativeVolume) {
        if (previousCumulativeVolume == 0) {
            previousCumulativeVolume = cumulativeVolume;
        }
        this.cumulativeVolume = cumulativeVolume;
    }

    public void update(int position, MarketBookOperation operation, MarketBookSide side, double price, int size) {
        List<MarketDepthItem> items = (side == MarketBookSide.Bid) ? bids : asks;
        switch (operation) {
            case Insert:
                items.add(position, new MarketDepthItem(size, price));
                break;
            case Update:
                MarketDepthItem item = items.get(position);
                item.setSize(size);
                item.setPrice(price);
                break;
            case Delete:
                if (position < items.size()) {
                    items.remove(position);
                }
                break;
        }

        if (operation == MarketBookOperation.Update && isValid()) {
            int cumulativeBid = getCumulativeSize(bids);
            int cumulativeAsk = getCumulativeSize(asks);
            double totalDepth = cumulativeBid + cumulativeAsk;

            lastBalance = 100d * (cumulativeBid - cumulativeAsk) / totalDepth;
            lowBalance = Math.min(lastBalance, lowBalance);
            highBalance = Math.max(lastBalance, highBalance);
        }
    }
}
