package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 */
public class MarketDepth {
    private final LinkedList<MarketDepthItem> bids, asks;
    private final LinkedList<Double> balances;
    private double averageBalance;
    private double midPointPrice;
    private final DecimalFormat df2;

    public MarketDepth() {
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
        balances = new LinkedList<Double>();
        df2 = NumberFormatterFactory.getNumberFormatter(2);
    }


    public void reset() {
        bids.clear();
        asks.clear();
        balances.clear();
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


    public synchronized void update(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
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
            if (!bids.isEmpty() && !asks.isEmpty()) {
                int cumulativeBid = getCumulativeSize(bids);
                int cumulativeAsk = getCumulativeSize(asks);
                double totalDepth = cumulativeBid + cumulativeAsk;

                double balance = 100.0d * (cumulativeBid - cumulativeAsk) / totalDepth;
                balances.add(balance);
                midPointPrice = (bids.getFirst().getPrice() + asks.getFirst().getPrice()) / 2;
            }
        }
    }


    public synchronized MarketSnapshot getMarketSnapshot(long time) {
        if (midPointPrice == 0) {
            // This is normal. It happens at the very start when market depth is initializing.
            return null;
        }

        if (!balances.isEmpty()) {
            double multiplier = 2.0 / (balances.size() + 1.0);
            for (double balance : balances) {
                averageBalance += (balance - averageBalance) * multiplier;
            }
        }

        double oneSecondBalance = Double.valueOf(df2.format(averageBalance));
        MarketSnapshot marketSnapshot = new MarketSnapshot(time, oneSecondBalance, midPointPrice);
        balances.clear();

        return marketSnapshot;
    }
}
