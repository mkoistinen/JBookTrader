package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.marketbook.*;

import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 */
public class MarketDepth {
    private final LinkedList<MarketDepthItem> bids, asks;
    private boolean isResetting;
    private double lowBalance, highBalance, lastBalance;
    private double midPointPrice;

    public MarketDepth() {
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
        isResetting = true;
    }

    public void reset() {
        isResetting = true;
        bids.clear();
        asks.clear();
    }

    public boolean isValid() {
        int bidLevels = bids.size();
        int askLevels = asks.size();

        if (bidLevels == 0 || askLevels == 0) {
            return false;
        }

        // The bid price of level N must be smaller or equal to the bid price of level N-1
        double previousLevelBidPrice = bids.getFirst().getPrice();
        for (int itemIndex = 1; itemIndex < bidLevels; itemIndex++) {
            double price = bids.get(itemIndex).getPrice();
            if (price > previousLevelBidPrice) {
                return false;
            } else {
                previousLevelBidPrice = price;
            }
        }

        // The ask price of level N must be greater or equal to the ask price of level N-1
        double previousLevelAskPrice = asks.getFirst().getPrice();
        for (int itemIndex = 1; itemIndex < askLevels; itemIndex++) {
            double price = asks.get(itemIndex).getPrice();
            if (price < previousLevelAskPrice) {
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

    public String getMarketDepthAsString() {
        return getCumulativeSize(bids) + "-" + getCumulativeSize(asks);
    }

    public void update(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
        List<MarketDepthItem> items = (side == MarketDepthSide.Bid) ? bids : asks;
        int levels = items.size();

        switch (operation) {
            case Insert:
                if (position <= levels) {
                    items.add(position, new MarketDepthItem(size, price));
                }
                break;
            case Update:
                if (position < levels) {
                    MarketDepthItem item = items.get(position);
                    item.setSize(size);
                    item.setPrice(price);
                }
                break;
            case Delete:
                if (position < levels) {
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
            midPointPrice = (bids.getFirst().getPrice() + asks.getFirst().getPrice()) / 2;
            isResetting = false;

        }
    }


    public MarketSnapshot getMarketSnapshot(long time) {
        if (isResetting) {
            return null;
        }

        int balance = (int) Math.round((lowBalance + highBalance) / 2d);
        MarketSnapshot marketSnapshot = new MarketSnapshot(time, balance, midPointPrice);
        // reset values for the next market snapshot
        highBalance = lowBalance = lastBalance;


        return marketSnapshot;
    }


}
