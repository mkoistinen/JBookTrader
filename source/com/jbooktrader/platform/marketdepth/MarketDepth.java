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


    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        Set<Double> uniquePriceLevels = new HashSet<Double>();
        int cumulativeSize = 0;

        for (MarketDepthItem item : items) {
            uniquePriceLevels.add(item.getPrice());
            cumulativeSize += item.getSize();
        }

        return cumulativeSize / uniquePriceLevels.size();
    }

    synchronized public void update(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
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


        if (operation == MarketDepthOperation.Update) {
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


    synchronized public MarketSnapshot getMarketSnapshot(long time) {
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
