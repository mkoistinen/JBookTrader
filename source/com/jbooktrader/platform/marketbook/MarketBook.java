package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.marketindex.*;

import java.util.*;

/**
 * Holds market depth history for a trading instrument.
 */
public class MarketBook {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketSnapshot> marketSnapshots;
    private final LinkedList<MarketDepthItem> bids, asks;
    private BackTestFileWriter backTestFileWriter;
    private String name;
    private TimeZone timeZone;
    private double lowBalance, highBalance, lastBalance;
    private int cumulativeVolume, previousCumulativeVolume;
    private double tick, trin, vix;

    public MarketBook() {
        marketSnapshots = new LinkedList<MarketSnapshot>();
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void save(MarketSnapshot marketSnapshot) {
        if (backTestFileWriter == null) {
            backTestFileWriter = new BackTestFileWriter(name, timeZone, true);
        }
        backTestFileWriter.write(marketSnapshot, true);
    }


    public List<MarketSnapshot> getAll() {
        return marketSnapshots;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MarketSnapshot marketSnapshot : marketSnapshots) {
            sb.append(marketSnapshot).append(LINE_SEP);
        }

        return sb.toString();
    }

    public int size() {
        return marketSnapshots.size();
    }

    public void add(MarketSnapshot marketSnapshot) {
        //todo: reset book and indicators at the start of the day for backtesting and optimization purposes
        marketSnapshots.add(marketSnapshot);
    }

    public MarketSnapshot getLastMarketSnapshot() {
        return marketSnapshots.getLast();
    }

    public MarketSnapshot getPreviousMarketSnapshot() {
        return marketSnapshots.get(marketSnapshots.size() - 2);
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

    public MarketSnapshot getNextMarketSnapshot(long time) {
        MarketSnapshot marketSnapshot = null;
        if (isValid()) {
            int volume = cumulativeVolume - previousCumulativeVolume;
            double bestBid = bids.getFirst().getPrice();
            double bestAsk = asks.getFirst().getPrice();
            marketSnapshot = new MarketSnapshot(time, (int) Math.round(lowBalance), (int) Math.round(highBalance), bestBid, bestAsk, volume,
                    tick, trin, vix);

            // initialize next market depth values
            previousCumulativeVolume = cumulativeVolume;
            highBalance = lowBalance = lastBalance;
        }

        return marketSnapshot;
    }

    public void updateIndex(MarketIndex marketIndex, double value) {
        switch (marketIndex) {
            case Tick:
                tick = value;
                break;
            case Trin:
                trin = value;
                break;
            case Vix:
                vix = value;
                break;
        }
    }

    public void updateVolume(int cumulativeVolume) {
        if (previousCumulativeVolume == 0) {
            previousCumulativeVolume = cumulativeVolume;
        }
        this.cumulativeVolume = cumulativeVolume;
    }

    public void updateDepth(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
        List<MarketDepthItem> items = (side == MarketDepthSide.Bid) ? bids : asks;
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

        if (operation == MarketDepthOperation.Update && isValid()) {
            int cumulativeBid = getCumulativeSize(bids);
            int cumulativeAsk = getCumulativeSize(asks);
            double totalDepth = cumulativeBid + cumulativeAsk;

            lastBalance = 100d * (cumulativeBid - cumulativeAsk) / totalDepth;
            lowBalance = Math.min(lastBalance, lowBalance);
            highBalance = Math.max(lastBalance, highBalance);
        }
    }
}
