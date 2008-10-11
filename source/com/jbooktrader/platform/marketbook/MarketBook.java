package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;

import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 */
public class MarketBook {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketSnapshot> marketSnapshots;
    private final LinkedList<MarketDepthItem> bids, asks;
    private final String name;
    private final TimeZone timeZone;
    private BackTestFileWriter backTestFileWriter;
    private boolean isResetting;
    private double lowBalance, highBalance, lastBalance;
    private double bestBid, bestAsk;


    public MarketBook(String name, TimeZone timeZone) {
        this.name = name;
        this.timeZone = timeZone;
        marketSnapshots = new LinkedList<MarketSnapshot>();
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
        isResetting = true;
    }

    public MarketBook() {
        this(null, null);
    }

    public void save(MarketSnapshot marketSnapshot) {
        if (backTestFileWriter == null) {
            try {
                backTestFileWriter = new BackTestFileWriter(name, timeZone, true);
            } catch (JBookTraderException e) {
                throw new RuntimeException(e);
            }
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
        isResetting = true;
        bids.clear();
        asks.clear();
    }

    public boolean isValid() {
        int bidLevels = bids.size();
        int askLevels = asks.size();
        if (bidLevels != askLevels || bidLevels == 0 || askLevels == 0) {
            // This may happen when the "delete" operation was performed,
            // but the "insert" operation was not yet completed, or vice versa.
            return false;
        }

        // The bid price of level N must be smaller than the bid price of level N-1
        double previousLevelBidPrice = bids.getFirst().getPrice();
        for (int itemIndex = 1; itemIndex < bidLevels; itemIndex++) {
            double price = bids.get(itemIndex).getPrice();
            if (price >= previousLevelBidPrice) {
                return false;
            } else {
                previousLevelBidPrice = price;
            }
        }

        // The ask price of level N must be greater than the ask price of level N-1
        double previousLevelAskPrice = asks.getFirst().getPrice();
        for (int itemIndex = 1; itemIndex < askLevels; itemIndex++) {
            double price = asks.get(itemIndex).getPrice();
            if (price <= previousLevelAskPrice) {
                return false;
            } else {
                previousLevelAskPrice = price;
            }
        }

        double bestBid = bids.getFirst().getPrice();
        double bestAsk = asks.getFirst().getPrice();
        return (bestBid < bestAsk);
    }

    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }

    public MarketSnapshot getNextMarketSnapshot(long time) {
        if (isResetting) {
            return null;
        }

        int balance = (int) Math.round((lowBalance + highBalance) / 2);
        MarketSnapshot marketSnapshot = new MarketSnapshot(time, balance, bestBid, bestAsk);
        // reset values for the next market snapshot
        highBalance = lowBalance = lastBalance;


        return marketSnapshot;
    }

    public void updateDepth(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
        List<MarketDepthItem> items = (side == MarketDepthSide.Bid) ? bids : asks;
        switch (operation) {
            case Insert:
                if (position <= items.size()) {
                    items.add(position, new MarketDepthItem(size, price));
                }
                break;
            case Update:
                if (position < items.size()) {
                    MarketDepthItem item = items.get(position);
                    item.setSize(size);
                    item.setPrice(price);
                }
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
            bestBid = bids.getFirst().getPrice();
            bestAsk = asks.getFirst().getPrice();
            isResetting = false;
        }
    }
}
